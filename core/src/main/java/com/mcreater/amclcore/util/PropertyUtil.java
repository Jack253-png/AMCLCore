package com.mcreater.amclcore.util;

public class PropertyUtil {
    public static String readProperty(String key) {
        return System.getProperty(key);
    }

    public static String readProperty(String key, String def) {
        return System.getProperty(key, def);
    }

    public static boolean readPropertyBoolean(String key) {
        return readPropertyBoolean(key, false);
    }

    public static boolean readPropertyBoolean(String key, boolean def) {
        return Boolean.parseBoolean(readProperty(key, String.valueOf(def)));
    }

    public static int readPropertyInteger(String key) {
        return readPropertyInteger(key, 0);
    }

    public static int readPropertyInteger(String key, int def) {
        try {
            return Integer.parseInt(readProperty(key, String.valueOf(def)));
        } catch (NumberFormatException e) {
            return def;
        }
    }

    public static long readPropertyLong(String key) {
        return readPropertyLong(key, 0);
    }

    public static long readPropertyLong(String key, long def) {
        try {
            return Long.parseLong(readProperty(key, String.valueOf(def)));
        } catch (NumberFormatException e) {
            return def;
        }
    }

    public static <E extends Enum<E>> E readPropertyEnum(Class<E> clazz, String key) {
        try {
            return Enum.valueOf(clazz, readProperty(key).toUpperCase());
        } catch (Exception e) {
            return null;
        }
    }

    public static void setProperty(String key, String value) {
        System.setProperty(key, value);
    }

    public static void setProperty(String key, boolean value) {
        System.setProperty(key, String.valueOf(value));
    }

    public static void setProperty(String key, int value) {
        System.setProperty(key, String.valueOf(value));
    }

    public static void setProperty(String key, long value) {
        System.setProperty(key, String.valueOf(value));
    }

    public static <E extends Enum<E>> void setProperty(String key, E value) {
        System.setProperty(key, value.name().toLowerCase());
    }
}
