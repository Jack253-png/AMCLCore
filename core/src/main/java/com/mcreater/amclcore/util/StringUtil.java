package com.mcreater.amclcore.util;

import com.mcreater.amclcore.util.iterators.RepeatableIterable;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.stream.Collectors;

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

    public static String shortString(String s, long size) {
        if (s.length() <= size) return s;
        return s.chars()
                .boxed()
                .map(integer -> String.valueOf((char) (int) integer))
                .limit(size)
                .collect(Collectors.joining()) + "...";
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
        if (uuid == null) return false;
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

    @NotNull
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
