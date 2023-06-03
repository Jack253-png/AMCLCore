package com.mcreater.amclcore.nbtlib.common.tags;

import com.mcreater.amclcore.util.ArrayUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public class ByteArrayTag extends ArrayTag<Byte> implements Comparable<ByteArrayTag> {
    public ByteArrayTag(Byte[] value) {
        super(value);
    }

    public ByteArrayTag(byte[] value) {
        super(ArrayUtils.boxed(value));
    }

    public ByteArrayTag clone() {
        return new ByteArrayTag(Arrays.copyOf(getValue(), size()));
    }

    public int compareTo(@NotNull ByteArrayTag o) {
        return Arrays.toString(getValue()).compareTo(Arrays.toString(o.getValue()));
    }
}
