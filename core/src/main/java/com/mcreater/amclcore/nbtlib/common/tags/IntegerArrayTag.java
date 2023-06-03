package com.mcreater.amclcore.nbtlib.common.tags;

import com.mcreater.amclcore.util.ArrayUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public class IntegerArrayTag extends ArrayTag<Integer> implements Comparable<IntegerArrayTag> {
    public IntegerArrayTag(Integer[] value) {
        super(value);
    }

    public IntegerArrayTag(int[] value) {
        super(ArrayUtils.boxed(value));
    }

    public IntegerArrayTag clone() {
        return new IntegerArrayTag(Arrays.copyOf(getValue(), size()));
    }

    public int compareTo(@NotNull IntegerArrayTag o) {
        return Arrays.toString(getValue()).compareTo(Arrays.toString(o.getValue()));
    }
}
