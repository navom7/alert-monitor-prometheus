package com.alertmonitor.controller;


import com.alertmonitor.model.kafka.ClientEventDTO;
import com.alertmonitor.service.MonitoringService;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/kafka")
public class KafkaController {

    @Autowired
    private MonitoringService monitoringService;

    @PostMapping("/publish/{topic}")
    public void sendMessageToKafkaTopic(@RequestBody ClientEventDTO clientEventDTO, @PathVariable("topic") String topic) throws JsonProcessingException {
//        producerService.sendEventToMonitoringService(topic, clientEventDTO);

        monitoringService.monitor(clientEventDTO);
    }
}
