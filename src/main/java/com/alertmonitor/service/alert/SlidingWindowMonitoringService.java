package com.alertmonitor.service.alert;

import com.alertmonitor.config.AlertConfigurations;
import com.alertmonitor.model.RedisObject;
import com.alertmonitor.model.kafka.ClientEventDTO;
import com.alertmonitor.service.AlertingService;
import com.alertmonitor.utils.Utils;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.IntStream;

import static com.alertmonitor.constants.AppConstants.SIMPLE_COUNT_EVENT_TYPE;
import static com.alertmonitor.constants.AppConstants.SLIDING_WINDOW_KEY_PREFIX;

@Service
@EnableAsync
@Slf4j
public class SlidingWindowMonitoringService extends MonitoringAbstractService {

    private final AlertConfigurations alertConfigurations;
    private final AlertingService alertingService;
    private final ConcurrentHashMap<String, Counter> eventCounters = new ConcurrentHashMap<>();
    private final ConcurrentLinkedQueue<Long> eventTimestamps = new ConcurrentLinkedQueue<>();

    @Autowired
    public SlidingWindowMonitoringService(AlertConfigurations alertConfigurations,
                                        AlertingService alertingService,
                                        MeterRegistry registry) {
        this.alertConfigurations = alertConfigurations;
        this.alertingService = alertingService;

        alertConfigurations.getAlertConfigList().forEach(alertConfig -> {
            eventCounters.computeIfAbsent(alertConfig.getEventType(), k -> Counter.builder(alertConfig.getEventType())
                    .description("Count of " + alertConfig.getEventType())
                    .register(registry));
        });
    }


    @Override
    @Async
    public void alert(ClientEventDTO clientEventDTO) throws JsonProcessingException {

        Counter eventCounter = eventCounters.get(clientEventDTO.getEventType());

        Optional<AlertConfigurations.AlertConfigList> optionalAlertConfig = alertConfigurations.getAlertConfigList().stream()
                .filter(config -> config.getEventType().equalsIgnoreCase(clientEventDTO.getEventType()))
                .findFirst();

        if (optionalAlertConfig.isPresent()) {
            AlertConfigurations.AlertConfigList alertConfig = optionalAlertConfig.get();


//            int idx = Utils.findAlertConfigIndex(alertConfigurations, clientEventDTO.getEventType());
//            if(idx == -1) {
//                log.error("Configuration for event type not found");
//            }
            eventCounter.increment();
            log.info("count: " + eventTimestamps.size());
            eventTimestamps.offer(Instant.now().toEpochMilli());
            if(isLimitBreached(alertConfig.getAlertConfig().getWindowSizeInSecs(), alertConfig.getAlertConfig().getCount())) {
                log.info("Client " + alertConfig.getClient() + " " + alertConfig.getEventType() + " threshold breached");
                alertingService.alert(alertConfig);
            } else {
                log.info("Client " + alertConfig.getClient() + " " + alertConfig.getEventType() + " " + alertConfig.getAlertConfig().getType());
            }

        } else {
            log.error("EventType not found!");
        }
    }

    public boolean isLimitBreached(long seconds, long limit) {
        long windowStartingTime = Instant.now().minusSeconds(seconds).toEpochMilli();
        eventTimestamps.removeIf(timestamp -> timestamp < windowStartingTime);

        if (eventTimestamps.size() > limit) {
            return true;
        }
        return false;
    }

}
