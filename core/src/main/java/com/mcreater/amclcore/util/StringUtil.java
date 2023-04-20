package com.mcreater.amclcore.util;

import com.mcreater.amclcore.util.iterators.RepeatableIterable;

import java.util.UUID;

public class StringUtil {
    public static String toPercentage(int value) {
        return String.format("%d%%", value);
    }

    public static String toStringedDouble(double value, int bit) {
        if (bit < 0) throw new IllegalArgumentException("argument 'bit' cannot be negative.");
        return String.format(String.format("%%.%df", bit), value);
    }

    public static String repeat(String s, int count) {
        if (count < 0) throw new IllegalArgumentException("argument 'count' cannot be negative.");
        return String.join("", new RepeatableIterable<>(s, count));
    }

    private static boolean internalCheckUUID(String uuid) {
        return uuid.toLowerCase().chars()
                .filter(value -> {
                    boolean isNumber = value >= 48 && value <= 57;
                    boolean isAlphabet = value >= 97 && value <= 102;
                    return isNumber || isAlphabet;
                })
                .count() == 32;
    }

    public static boolean checkUUID(String uuid) {
        switch (uuid.length()) {
            case 32:
                return internalCheckUUID(uuid);
            case 36:
                return internalCheckUUID(toNoLineUUID(uuid));
            default:
                return false;
        }
    }

    public static boolean isLineUUID(String s) {
        return s.contains("-");
    }

    public static String toNoLineUUID(UUID uuid) {
        return uuid.toString().replace("-", "");
    }

    public static String toNoLineUUID(String uuid) {
        return uuid.replace("-", "");
    }

    public static UUID toLineUUID(String uuid) {
        if (!checkUUID(uuid)) return null;
        String s1 = uuid.substring(0, 8);
        String s2 = uuid.substring(8, 13);
        String s3 = uuid.substring(13, 18);
        String s4 = uuid.substring(18, 23);
        String s5 = uuid.substring(23);
        return UUID.fromString(String.join("-", s1, s2, s3, s4, s5));
    }
}
