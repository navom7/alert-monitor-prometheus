package com.alertmonitor.service.alert;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class EventService {

    private final Counter eventTypeCounter;

    @Autowired
    public EventService(MeterRegistry registry) {
        eventTypeCounter = Counter.builder("event_type")
                .description("Count of event types")
                .tags("type", "PAYMENT_EXCEPTION")
                .register(registry);
    }

    public void recordEvent(String eventType) {
        eventTypeCounter.increment();
    }
}
