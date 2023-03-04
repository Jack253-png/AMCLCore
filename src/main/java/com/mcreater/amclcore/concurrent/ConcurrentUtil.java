package com.mcreater.amclcore.concurrent;

public class ConcurrentUtil {
    public static void sleepTime(long millis) {
        try {
            Thread.sleep(millis);
        }
        catch (Exception ignored) {}
    }

    public static Object createLook() {
        return new Object();
    }
}
