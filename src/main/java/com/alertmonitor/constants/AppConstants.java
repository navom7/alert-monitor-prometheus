package com.alertmonitor.constants;

import com.alertmonitor.config.AlertConfigurations;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(AlertConfigurations.class)
public class AppConstants {
    public static final Long REDIS_TIMEOUT_HOURS = 24L;
    public static final String SIMPLE_COUNT_EVENT_TYPE = "SIMPLE_COUNT_EVENT_TYPE";
    public static final String SLIDING_WINDOW_EVENT_TYPE = "SLIDING_WINDOW_EVENT_TYPE";
    public static final String TUMBLING_WINDOW_KEY_PREFIX = "TUMBLING_WINDOW_";
    public static final String SLIDING_WINDOW_KEY_PREFIX = "SLIDING_WINDOW_";
}
