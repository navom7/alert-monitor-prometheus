package com.alertmonitor.service.alert;

import com.alertmonitor.model.kafka.ClientEventDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;

@Service
@EnableAsync
public interface MonitoringService {
    @Async
    public abstract void alert(ClientEventDTO clientEventDTO) throws JsonProcessingException;

}
