package com.mcreater.amclcore.concurrent;

import com.mcreater.amclcore.exceptions.report.ExceptionReporter;

public class ConcurrentUtil {
    public static void sleepTime(long millis) {
        try {
            Thread.sleep(millis);
        } catch (Exception e) {
            ExceptionReporter.report(e, ExceptionReporter.ExceptionType.CONCURRENT);
        }
    }

    public static Object createLook() {
        return new Object();
    }
}
