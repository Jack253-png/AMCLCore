package com.mcreater.amclcore.util.iterators;

import lombok.AllArgsConstructor;

import java.util.Iterator;

@AllArgsConstructor
public class RepeatableIterable<T> implements Iterable<T> {
    private final T data;
    private final int count;

    public Iterator<T> iterator() {
        return new RepeatableIterator<>(data, count);
    }
}
