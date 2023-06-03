package com.mcreater.amclcore.nbtlib.common.tags;

import org.jetbrains.annotations.NotNull;

public class IntegerTag extends NumberTag<Integer> implements Comparable<IntegerTag> {
    public IntegerTag(int value) {
        super(value);
    }

    public IntegerTag(Integer value) {
        super(value);
    }

    public IntegerTag clone() {
        return new IntegerTag(getValue());
    }

    public int compareTo(@NotNull IntegerTag o) {
        return this.getValue().compareTo(o.getValue());
    }
}
