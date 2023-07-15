package org.wzm.utils;

import java.util.Calendar;
import java.util.Date;

public class DateUtil {
    public static final int MINUTE       = Calendar.MINUTE;
    public static final int DAY_OF_MONTH = Calendar.DAY_OF_MONTH;

    private DateUtil() {
    }

    public static long getCurrentTimestamp() {
        return System.currentTimeMillis();
    }

    public static Date getCurrentTime() {
        long currentTime = getCurrentTimestamp();
        return new Date(currentTime);
    }

    public static Date getTimeAfter(int field, int amount) {
        Calendar instance = Calendar.getInstance();
        instance.setTime(new Date());
        instance.add(field, amount);
        return instance.getTime();
    }
}
