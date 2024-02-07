package com.alertmonitor.service.alert;

import com.alertmonitor.config.AlertConfigurations;
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

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

import static com.alertmonitor.constants.AppConstants.SIMPLE_COUNT_EVENT_TYPE;

@Slf4j
@EnableAsync
@Service
public class SimpleCountMonitoringService extends MonitoringAbstractService {

    private final AlertConfigurations alertConfigurations;
    private final AlertingService alertingService;
    private final ConcurrentHashMap<String, Counter> eventCounters = new ConcurrentHashMap<>();

    private final ConcurrentLinkedQueue<Long> eventTimestamps = new ConcurrentLinkedQueue<>();

    @Autowired
    public SimpleCountMonitoringService(AlertConfigurations alertConfigurations,
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


    @Async
    @Override
    public void alert(ClientEventDTO clientEventDTO) throws JsonProcessingException {
        Counter eventCounter = eventCounters.get(clientEventDTO.getEventType());

        Optional<AlertConfigurations.AlertConfigList> optionalAlertConfig = alertConfigurations.getAlertConfigList().stream()
                .filter(config -> config.getEventType().equalsIgnoreCase(clientEventDTO.getEventType()))
                .findFirst();
        if (optionalAlertConfig.isPresent()) {
            AlertConfigurations.AlertConfigList alertConfig = optionalAlertConfig.get();

            int idx = Utils.findAlertConfigIndex(alertConfigurations, clientEventDTO.getEventType());
            if(idx == -1) {
                log.error("Configuration for event type not found");
            }
            eventCounter.increment();

            double eventCount = eventCounter.count();
            if(Utils.hasValue(eventCount)) {
                if(eventCount >= alertConfig.getAlertConfig().getCount()) {
                    log.info("Client " + alertConfig.getClient() + " " + alertConfig.getEventType() + " threshold breached");
                    alertingService.alert(alertConfig);
                } else {
                    log.info("Client " + alertConfig.getClient() + " " + alertConfig.getEventType() + " " + alertConfig.getAlertConfig().getType());
                }
            } else {
                log.info("Client " + alertConfig.getClient() + " " + alertConfig.getEventType() + " " + alertConfig.getAlertConfig().getType());
            }
        } else {
            log.error("EventType not found!");
        }
    }




}
