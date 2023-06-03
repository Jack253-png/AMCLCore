package com.mcreater.amclcore.nbtlib.common.tags;

import org.jetbrains.annotations.NotNull;

public class LongTag extends NumberTag<Long> implements Comparable<LongTag> {
    public LongTag(Long value) {
        super(value);
    }

    public LongTag(long value) {
        super(value);
    }

    public LongTag clone() {
        return new LongTag(getValue());
    }

    public int compareTo(@NotNull LongTag o) {
        return getValue().compareTo(o.getValue());
    }
}
