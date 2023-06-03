package com.mcreater.amclcore.util.date;

import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;

public class DateUtil {
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

    public static String toDate(Date date) {
        return DATE_FORMAT.format(date);
    }

    public static long dateBetween(Date nextDay, Date nowDay) {
        return ChronoUnit.DAYS.between(nowDay.toInstant().atZone(ZoneId.systemDefault()), nextDay.toInstant().atZone(ZoneId.systemDefault()));
    }
}
