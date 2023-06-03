package com.mcreater.amclcore.nbtlib.common.tags;

import org.jetbrains.annotations.NotNull;

public class StringTag extends AbstractTag<String> implements Comparable<StringTag> {
    public StringTag(String value) {
        super(value);
    }

    public StringTag clone() {
        return new StringTag(getValue());
    }

    public int compareTo(@NotNull StringTag o) {
        return this.getValue().compareTo(o.getValue());
    }
}
