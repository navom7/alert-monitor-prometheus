package com.alertmonitor.utils;


import com.alertmonitor.config.AlertConfigurations;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;


@Component
public class Utils {

    public static String getAlertType(String eventType) {
        //if this key value pairs increases with time, we can add this to redis cache also

        switch (eventType) {

            case "PAYMENT_EXCEPTION":
                return "TUMBLING_WINDOW";

            case "USERSERVICE_EXCEPTION1":
                return "SLIDING_WINDOW";

            case "USERSERVICE_EXCEPTION2":
                return "SIMPLE_COUNT";

            default:
                return "NOT_FOUND";
        }
    }


    public static boolean hasValue(Object obj) {

        if (obj == null) {
            return false;
        }
        if (obj instanceof String) {
            return !((String) obj).isEmpty();
        }
        if (obj instanceof Collection) {
            return !((Collection<?>) obj).isEmpty();
        }
        if (obj instanceof Map) {
            return !((Map<?, ?>) obj).isEmpty();
        }
        return true;
    }


    public static boolean hasRecords(List<Object> obj) {
        if (obj == null || obj.isEmpty()) {
            return false;
        }
        for (Object item : obj) {
            if (item != null) {
                return true;
            }
        }
        return false;
    }

    public static long getNextEpochTimeForTumblingWindow(long seconds){
        long currentEpochTime = System.currentTimeMillis() / 1000;

        long remainder = currentEpochTime % seconds;

        long secondsToAdd = (remainder == 0) ? seconds : (seconds - remainder);

        return secondsToAdd;
    }

    public static long getTimeForAllowedTimeWindow(long seconds) {
        long currentEpochTime = System.currentTimeMillis()/1000;
        return currentEpochTime - seconds;
    }

    public static int findAlertConfigIndex(AlertConfigurations alertConfigurations, String eventType) {
        OptionalInt indexOpt = IntStream.range(0, alertConfigurations.getAlertConfigList().size())
                .filter(i -> alertConfigurations.getAlertConfigList().get(i).getEventType().equalsIgnoreCase(eventType))
                .findFirst();

        return indexOpt.orElse(-1);
    }
}


