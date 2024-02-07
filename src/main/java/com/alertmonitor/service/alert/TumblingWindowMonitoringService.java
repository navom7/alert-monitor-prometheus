package com.alertmonitor.service.alert;

import com.alertmonitor.config.AlertConfigurations;
import com.alertmonitor.model.kafka.ClientEventDTO;
import com.alertmonitor.service.AlertingService;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

import static com.alertmonitor.constants.AppConstants.TUMBLING_WINDOW_KEY_PREFIX;

@Service
@EnableAsync
@Slf4j
public class TumblingWindowMonitoringService extends MonitoringAbstractService {

    private final AlertConfigurations alertConfigurations;
    private final AlertingService alertingService;
    private final ConcurrentLinkedQueue<Long> eventTimestamps = new ConcurrentLinkedQueue<>();

    private final ConcurrentHashMap<String, AtomicInteger> eventCounters = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Integer> previousIntervalCounts = new ConcurrentHashMap<>();


    @Autowired
    public TumblingWindowMonitoringService(AlertConfigurations alertConfigurations,
                                          AlertingService alertingService,
                                          MeterRegistry registry) {
        this.alertConfigurations = alertConfigurations;
        this.alertingService = alertingService;

        alertConfigurations.getAlertConfigList().forEach(alertConfig -> {
            eventCounters.computeIfAbsent(alertConfig.getEventType(), k -> new AtomicInteger(0));
        });
    }


    @Override
    @Async
    public void alert(ClientEventDTO clientEventDTO) throws JsonProcessingException {

        AtomicInteger eventCounter = eventCounters.computeIfAbsent(clientEventDTO.getEventType(), k -> new AtomicInteger(0));

        int currentCount = eventCounter.incrementAndGet();

        Optional<AlertConfigurations.AlertConfigList> optionalAlertConfig = alertConfigurations.getAlertConfigList().stream()
                .filter(config -> config.getEventType().equalsIgnoreCase(clientEventDTO.getEventType()))
                .findFirst();

        if (optionalAlertConfig.isPresent()) {
            AlertConfigurations.AlertConfigList alertConfig = optionalAlertConfig.get();

            if(currentCount >= alertConfig.getAlertConfig().getCount()) {
                log.info("Client " + alertConfig.getClient() + " " + alertConfig.getEventType() + " threshold breached");
                alertingService.alert(alertConfig);
            } else {
                log.info("Client " + alertConfig.getClient() + " " + alertConfig.getEventType() + " " + alertConfig.getAlertConfig().getType());
            }
        } else {
            log.error("EventType not found!");
        }
    }

    @Scheduled(fixedRate = 10000) // 10,000 milliseconds = 10 seconds
    public void resetEventCounters() {
        eventCounters.forEach((eventType, counter) -> {
            int count = counter.getAndSet(0); // Reset for the next interval
            previousIntervalCounts.put(eventType, count);
            if (count > 10) {
                System.out.println("Event count for " + eventType + " exceeded 10 in the last 10 seconds. Count was: " + count);
            }
        });
    }

}
