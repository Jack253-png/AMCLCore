package com.mcreater.amclcore.nbtlib.common.tags;

import java.util.Arrays;
import java.util.Iterator;

public abstract class ArrayTag<T extends Comparable<T>> extends AbstractTag<T[]> implements Iterable<T> {
    protected ArrayTag(T[] value) {
        super(value);
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
