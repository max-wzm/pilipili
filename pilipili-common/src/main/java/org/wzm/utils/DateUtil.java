package org.wzm.utils;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

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

    public static String formatDate(Date date, String patter) {
        return DateTimeFormatter.ofPattern(patter).format(date2LocalDate(date));
    }

    public static Date parseDate(long timestamp) {
        return new Date(timestamp);
    }

    public static LocalDate date2LocalDate(Date date) {
        if (Objects.isNull(date)) {
            return null;
        }
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    public static int getDayOfMonth(Date date) {
        return date2LocalDate(DateUtil.getCurrentTime()).getDayOfMonth();
    }
}
