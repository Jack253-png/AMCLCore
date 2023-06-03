package com.mcreater.amclcore.nbt;

import com.mcreater.amclcore.nbt.tags.*;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

@AllArgsConstructor
public enum TagType {
    BYTE_TAG((byte) 1, ByteTag.class, o -> new ByteTag((byte) o)),
    SHORT_TAG((byte) 2, ShortTag.class, o -> new ShortTag((short) o)),
    INTEGER_TAG((byte) 3, IntegerTag.class, o -> new IntegerTag((int) o)),
    LONG_TAG((byte) 4, LongTag.class, o -> new LongTag((long) o)),
    FLOAT_TAG((byte) 5, FloatTag.class, o -> new FloatTag((float) o)),
    DOUBLE_TAG((byte) 6, DoubleTag.class, o -> new DoubleTag((double) o)),
    BYTE_ARRAY_TAG((byte) 7, ByteArrayTag.class, o -> new ByteArrayTag((byte[]) o)),
    STRING_TAG((byte) 8, StringTag.class, o -> new StringTag((String) o)),
    LIST_TAG((byte) 9, ListTag.class, o -> {
        Map.Entry<TagType, List<AbstractTag<?>>> ent = (Map.Entry<TagType, List<AbstractTag<?>>>) o;
        return new ListTag<>(ent.getKey().getClazz(), ent.getValue());
    });
    private final byte id;
    @Getter
    private final Class<?> clazz;
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
