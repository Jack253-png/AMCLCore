package com.mcreater.amclcore.nbt.tags;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.Vector;

public class ByteArrayTag extends ArrayTag<Byte> implements Comparable<ByteArrayTag> {
    public ByteArrayTag(Byte[] value) {
        super(value);
    }

    public ByteArrayTag(byte[] value) {
        super(new Byte[0]);
        List<Byte> a = new Vector<>();
        for (byte b : value) a.add(b);
        setValue(a.toArray(new Byte[0]));
    }

    public ByteArrayTag clone() {
        return new ByteArrayTag(Arrays.copyOf(getValue(), size()));
    }

    public int compareTo(@NotNull ByteArrayTag o) {
        return Arrays.toString(getValue()).compareTo(Arrays.toString(o.getValue()));
    }
}
