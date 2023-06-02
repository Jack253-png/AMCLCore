package com.mcreater.amclcore.nbt.tags;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class StringTag extends AbstractTag<String> implements Comparable<StringTag> {
    public StringTag(String value) {
        super(value);
    }

    public String toString() {
        return Objects.toString(getValue());
    }

    public StringTag clone() {
        return new StringTag(getValue());
    }

    public int compareTo(@NotNull StringTag o) {
        return this.getValue().compareTo(o.getValue());
    }
}
