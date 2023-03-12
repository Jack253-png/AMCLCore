package com.mcreater.amclcore.util;

public class PropertyUtil {
    public static String readProperty(String key) {
        return System.getProperty(key);
    }

    public static String readProperty(String key, String def) {
        return System.getProperty(key, def);
    }
}
