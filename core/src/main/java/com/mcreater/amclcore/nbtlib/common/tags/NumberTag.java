package com.mcreater.amclcore.nbtlib.common.tags;

public abstract class NumberTag<T extends Number & Comparable<T>> extends AbstractTag<T> {
    protected NumberTag(T value) {
        super(value);
    }

    public byte asByte() {
        return getValue().byteValue();
    }

    public short asShort() {
        return getValue().shortValue();
    }

    public int asInteger() {
        return getValue().intValue();
    }

    public long asLong() {
        return getValue().longValue();
    }

    public float asFloat() {
        return getValue().floatValue();
    }

    public double asDouble() {
        return getValue().doubleValue();
    }
}
