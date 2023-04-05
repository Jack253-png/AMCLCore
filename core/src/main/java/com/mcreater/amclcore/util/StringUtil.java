package com.mcreater.amclcore.util;

import com.mcreater.amclcore.util.iterators.RepeatableIterable;

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
}
