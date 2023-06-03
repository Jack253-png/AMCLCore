package com.mcreater.amclcore.nbtlib.common.tags;

import org.jetbrains.annotations.NotNull;

public class DoubleTag extends NumberTag<Double> implements Comparable<DoubleTag> {
    public DoubleTag(Double value) {
        super(value);
    }

    public DoubleTag(double value) {
        super(value);
    }

    public DoubleTag clone() {
        return new DoubleTag(getValue());
    }

    public int compareTo(@NotNull DoubleTag o) {
        return getValue().compareTo(o.getValue());
    }
}
