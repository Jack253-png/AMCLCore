package com.mcreater.amclcore.nbtlib.common.tags;

import org.jetbrains.annotations.NotNull;

public class ShortTag extends NumberTag<Short> implements Comparable<ShortTag> {
    public ShortTag(Short value) {
        super(value);
    }

    public ShortTag(short value) {
        super(value);
    }

    public ShortTag clone() {
        return new ShortTag(getValue());
    }

    public int compareTo(@NotNull ShortTag o) {
        return getValue().compareTo(o.getValue());
    }
}
