package com.mcreater.amclcore.nbtlib.common.tags;

import org.jetbrains.annotations.NotNull;

public class FloatTag extends NumberTag<Float> implements Comparable<FloatTag> {
    public FloatTag(Float value) {
        super(value);
    }

    public FloatTag(float value) {
        super(value);
    }

    public FloatTag clone() {
        return new FloatTag(getValue());
    }

    public int compareTo(@NotNull FloatTag o) {
        return getValue().compareTo(o.getValue());
    }
}
