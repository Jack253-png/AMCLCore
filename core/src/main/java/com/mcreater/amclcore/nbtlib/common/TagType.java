package com.mcreater.amclcore.nbtlib.common;

import com.mcreater.amclcore.nbtlib.common.tags.*;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

@AllArgsConstructor
public enum TagType {
    END_TAG((byte) 0, EndTag.class, o -> EndTag.INSTANCE),
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
        return new ListTag<>((Class<AbstractTag<?>>) ent.getKey().getClazz(), ent.getValue());
    }),
    COMPOUND_TAG((byte) 10, CompoundTag.class, o -> new CompoundTag((Map<String, AbstractTag<?>>) o)),
    INTEGER_ARRAY_TAG((byte) 11, IntegerArrayTag.class, o -> new IntegerArrayTag((int[]) o)),
    LONG_ARRAY_TAG((byte) 12, LongArrayTag.class, o -> new LongArrayTag((long[]) o));
    @Getter
    private final byte id;
    @Getter
    private final Class<?> clazz;
    private final Function<Object, AbstractTag<?>> generator;

    /**
     * generate a tag<br>生成一个标签<br>
     * end tag -> {@code null}<br>
     * byte tag -> {@code byte}<br>
     * short tag -> {@code short}<br>
     * integer tag -> {@code int}<br>
     * long tag -> {@code long}<br>
     * float tag -> {@code float}<br>
     * double tag -> {@code double}<br>
     * byte array tag -> {@code byte[]}<br>
     * string tag -> {@code String}<br>
     * list tag -> {@code Map.Entry<TagType, List<AbstractTag<?>>>}<br>
     * compound tag -> {@code Map<String, AbstractTag<?>>}<br>
     * integer array tag -> {@code int[]}<br>
     * long array tag -> {@code long[]}<br>
     * --------------------------------<br>
     * 结尾标签 -> {@code null}<br>
     * 字节标签 -> {@code byte}<br>
     * 短整数标签 -> {@code short}<br>
     * 整数标签 -> {@code int}<br>
     * 长整数 -> {@code long}<br>
     * 单精度浮点数标签 -> {@code float}<br>
     * 双精度浮点数标签 -> {@code double}<br>
     * 字节数组标签 -> {@code byte[]}<br>
     * 字符串标签 -> {@code String}<br>
     * 列表标签 -> {@code Map.Entry<TagType, List<AbstractTag<?>>>}<br>
     * 节点标签 -> {@code Map<String, AbstractTag<?>>}<br>
     * 整数数组标签 -> {@code int[]}<br>
     * 长整数数组标签 -> {@code long[]}<br>
     *
     * @return the generated tag<br>生成的标签
     */
    public AbstractTag<?> generate(Object value) {
        return generator.apply(value);
    }

    public static TagType search(byte b) {
        for (TagType t : values()) {
            if (t.id == b) return t;
        }
        return null;
    }

    public static TagType search(Class<?> b) {
        for (TagType t : values()) {
            if (t.getClazz() == b) return t;
        }
        return null;
    }
}
