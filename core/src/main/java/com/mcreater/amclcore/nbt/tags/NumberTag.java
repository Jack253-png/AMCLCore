package com.mcreater.amclcore.nbt.tags;

import java.util.Objects;

public abstract class NumberTag<T extends Number & Comparable<T>> extends AbstractTag<T> {
    protected NumberTag(T value) {
        super(value);
    }

    public String toString() {
        return Objects.toString(getValue());
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
