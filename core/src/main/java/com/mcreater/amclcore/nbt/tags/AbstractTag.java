package com.mcreater.amclcore.nbt.tags;

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

    public abstract String toString();

    /**
     * clone this tag<br>复制一个标签
     *
     * @return the cloned tag<br>返回复制的标签
     */
    public abstract AbstractTag<T> clone();

}
