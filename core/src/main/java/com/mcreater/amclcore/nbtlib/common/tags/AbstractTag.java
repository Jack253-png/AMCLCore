package com.mcreater.amclcore.nbtlib.common.tags;

import com.mcreater.amclcore.util.JsonUtil;
import lombok.Getter;
import lombok.Setter;

public abstract class AbstractTag<T> implements Cloneable {
    @Getter
    @Setter
    private T value;

    protected AbstractTag(T value) {
        this.value = value;
    }

    /**
     * default max depth for NBT<br>NBT 默认最大递归深度
     */
    public static final int DEFAULT_MAX_DEPTH = 512;

    public boolean equals(Object obj) {
        return obj != null && getClass() == obj.getClass();
    }

    public int hashCode() {
        return value.hashCode();
    }

    public String toString() {
        return JsonUtil.GSON_PARSER.toJson(this);
    }

    /**
     * clone this tag<br>复制一个标签
     *
     * @return the cloned tag<br>返回复制的标签
     */
    public abstract AbstractTag<T> clone();

    public EndTag toEndTag() {
        return (EndTag) this;
    }

    public <T extends Number & Comparable<T>> NumberTag<T> toNumberTag(Class<T> clazz) {
        return (NumberTag<T>) this;
    }

    public NumberTag<?> toNumberTag() {
        return (NumberTag<?>) this;
    }

    public <T extends AbstractTag<?>> ListTag<T> toListTag(Class<T> clazz) {
        return (ListTag<T>) this;
    }

    public ListTag<?> toListTag() {
        return (ListTag<?>) this;
    }

    public CompoundTag toCompoundTag() {
        return (CompoundTag) this;
    }

    public ByteArrayTag toByteArrayTag() {
        return (ByteArrayTag) this;
    }

    public IntegerArrayTag toIntegerArrayTag() {
        return (IntegerArrayTag) this;
    }

    public LongArrayTag toLongArrayTag() {
        return (LongArrayTag) this;
    }

    public StringTag toStringTag() {
        return (StringTag) this;
    }
}
