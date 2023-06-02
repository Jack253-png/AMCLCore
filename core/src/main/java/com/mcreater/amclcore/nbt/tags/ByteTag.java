package com.mcreater.amclcore.nbt.tags;

import org.jetbrains.annotations.NotNull;

public class ByteTag extends NumberTag<Byte> implements Comparable<ByteTag> {
    public ByteTag(byte b) {
        super(b);
    }

    public ByteTag(Byte b) {
        super(b);
    }

    public ByteTag clone() {
        return new ByteTag(getValue());
    }

    public int compareTo(@NotNull ByteTag o) {
        return getValue().compareTo(o.getValue());
    }
}
