package com.mcreater.amclcore.nbtlib.common.tags;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.Vector;

public class IntegerArrayTag extends ArrayTag<Integer> implements Comparable<IntegerArrayTag> {
    public IntegerArrayTag(Integer[] value) {
        super(value);
    }

    public IntegerArrayTag(int[] value) {
        super(new Integer[0]);
        List<Integer> a = new Vector<>();
        for (int b : value) a.add(b);
        setValue(a.toArray(new Integer[0]));
    }

    public IntegerArrayTag clone() {
        return new IntegerArrayTag(Arrays.copyOf(getValue(), size()));
    }

    public int compareTo(@NotNull IntegerArrayTag o) {
        return Arrays.toString(getValue()).compareTo(Arrays.toString(o.getValue()));
    }
}
