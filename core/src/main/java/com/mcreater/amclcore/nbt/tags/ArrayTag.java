package com.mcreater.amclcore.nbt.tags;

import java.util.Arrays;
import java.util.Iterator;

public abstract class ArrayTag<T extends Comparable<T>> extends AbstractTag<T[]> implements Iterable<T> {
    public ArrayTag(T[] value) {
        super(value);
    }

    public String toString() {
        return Arrays.toString(getValue());
    }

    public Iterator<T> iterator() {
        return Arrays.stream(getValue()).iterator();
    }

    public int size() {
        return getValue().length;
    }

    public int hashCode() {
        return Arrays.hashCode(getValue());
    }
}
