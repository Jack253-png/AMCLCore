package com.mcreater.amclcore.nbt;

import com.mcreater.amclcore.nbt.tags.*;
import lombok.AllArgsConstructor;

import java.util.function.Function;

@AllArgsConstructor
public enum TagType {
    BYTE_TAG((byte) 1, o -> new ByteTag((byte) o)),
    SHORT_TAG((byte) 2, o -> new ShortTag((short) o)),
    INTEGER_TAG((byte) 3, o -> new IntegerTag((int) o)),
    LONG_TAG((byte) 4, o -> new LongTag((long) o)),
    FLOAT_TAG((byte) 5, o -> new FloatTag((float) o)),
    DOUBLE_TAG((byte) 6, o -> new DoubleTag((double) o)),
    STRING_TAG((byte) 8, o -> new StringTag((String) o));
    private final byte id;
    private final Function<Object, AbstractTag<?>> generator;

    public AbstractTag<?> generate(Object value) {
        return generator.apply(value);
    }

    public static TagType search(byte b) {
        for (TagType t : values()) {
            if (t.id == b) return t;
        }
        return null;
    }
}
