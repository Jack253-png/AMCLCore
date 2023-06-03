package com.mcreater.amclcore.nbtlib.common.tags;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.Vector;

public class LongArrayTag extends ArrayTag<Long> implements Comparable<LongArrayTag> {
    public LongArrayTag(Long[] value) {
        super(value);
    }

    public LongArrayTag(long[] value) {
        super(new Long[0]);
        List<Long> a = new Vector<>();
        for (long b : value) a.add(b);
        setValue(a.toArray(new Long[0]));
    }

    public LongArrayTag clone() {
        return new LongArrayTag(Arrays.copyOf(getValue(), size()));
    }

    public int compareTo(@NotNull LongArrayTag o) {
        return Arrays.toString(getValue()).compareTo(Arrays.toString(o.getValue()));
    }
}
