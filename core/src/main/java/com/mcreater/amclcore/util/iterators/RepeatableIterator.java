package com.mcreater.amclcore.util.iterators;

import java.util.Iterator;

public class RepeatableIterator<T> implements Iterator<T> {
    private final T data;
    private final int count;
    private int current;

    public RepeatableIterator(T data, int count) {
        this.data = data;
        this.count = count;
    }

    public boolean hasNext() {
        return count - 1 > current;
    }

    public T next() {
        current++;
        return data;
    }
}
