package com.mcreater.amclcore.nbtlib.common.tags;

import com.mcreater.amclcore.util.ArrayUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public class LongArrayTag extends ArrayTag<Long> implements Comparable<LongArrayTag> {
    public LongArrayTag(Long[] value) {
        super(value);
    }

    public LongArrayTag(long[] value) {
        super(ArrayUtils.boxed(value));
    }

    public LongArrayTag clone() {
        return new LongArrayTag(Arrays.copyOf(getValue(), size()));
    }

    public int compareTo(@NotNull LongArrayTag o) {
        return Arrays.toString(getValue()).compareTo(Arrays.toString(o.getValue()));
    }
}
