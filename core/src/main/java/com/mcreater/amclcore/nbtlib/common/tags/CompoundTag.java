package com.mcreater.amclcore.nbtlib.common.tags;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.mcreater.amclcore.util.ArrayUtils.debox;

public class CompoundTag extends AbstractTag<Map<String, AbstractTag<?>>> implements Iterable<Map.Entry<String, AbstractTag<?>>>, Comparable<CompoundTag>, Map<String, AbstractTag<?>> {
    public CompoundTag(Map<String, AbstractTag<?>> value) {
        super(value);
    }

    public CompoundTag clone() {
        return new CompoundTag(getValue().entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
    }

    public int compareTo(@NotNull CompoundTag o) {
        return getValue().hashCode() - o.getValue().hashCode();
    }

    @NotNull
    public Iterator<Entry<String, AbstractTag<?>>> iterator() {
        return getValue().entrySet().iterator();
    }

    public NumberTag<?> getNumberTag(String key) {
        return (NumberTag<?>) getValue().get(key);
    }

    public Number getNumber(String key) {
        return getNumberTag(key).getValue();
    }

    public ByteTag getByteTag(String key) {
        return get(key, ByteTag.class);
    }

    public ShortTag getShortTag(String key) {
        return get(key, ShortTag.class);
    }

    public IntegerTag getIntTag(String key) {
        return get(key, IntegerTag.class);
    }

    public LongTag getLongTag(String key) {
        return get(key, LongTag.class);
    }

    public FloatTag getFloatTag(String key) {
        return get(key, FloatTag.class);
    }

    public DoubleTag getDoubleTag(String key) {
        return get(key, DoubleTag.class);
    }

    public StringTag getStringTag(String key) {
        return get(key, StringTag.class);
    }

    public ByteArrayTag getByteArrayTag(String key) {
        return get(key, ByteArrayTag.class);
    }

    public IntegerArrayTag getIntArrayTag(String key) {
        return get(key, IntegerArrayTag.class);
    }

    public LongArrayTag getLongArrayTag(String key) {
        return get(key, LongArrayTag.class);
    }

    public ListTag<?> getListTag(String key) {
        return get(key, ListTag.class);
    }

    public CompoundTag getCompoundTag(String key) {
        return get(key, CompoundTag.class);
    }

    public boolean getBoolean(String key) {
        AbstractTag<?> t = get(key);
        return t instanceof ByteTag && ((ByteTag) t).asByte() > 0;
    }

    public byte getByte(String key) {
        ByteTag t = getByteTag(key);
        return t == null ? 0 : t.asByte();
    }

    public short getShort(String key) {
        ShortTag t = getShortTag(key);
        return t == null ? 0 : t.asShort();
    }

    public int getInt(String key) {
        IntegerTag t = getIntTag(key);
        return t == null ? 0 : t.asInteger();
    }

    public long getLong(String key) {
        LongTag t = getLongTag(key);
        return t == null ? 0 : t.asLong();
    }

    public float getFloat(String key) {
        FloatTag t = getFloatTag(key);
        return t == null ? 0 : t.asFloat();
    }

    public double getDouble(String key) {
        DoubleTag t = getDoubleTag(key);
        return t == null ? 0 : t.asDouble();
    }

    public String getString(String key) {
        StringTag t = getStringTag(key);
        return t == null ? "" : t.getValue();
    }

    public byte[] getByteArray(String key) {
        ByteArrayTag t = getByteArrayTag(key);
        return t == null ? new byte[0] : debox(t.getValue());
    }

    public int[] getIntArray(String key) {
        IntegerArrayTag t = getIntArrayTag(key);
        return t == null ? new int[0] : debox(t.getValue());
    }

    public long[] getLongArray(String key) {
        LongArrayTag t = getLongArrayTag(key);
        return t == null ? new long[0] : debox(t.getValue());
    }

    public AbstractTag<?> putBoolean(String key, boolean value) {
        return put(key, new ByteTag((byte) (value ? 1 : 0)));
    }

    public AbstractTag<?> putByte(String key, byte value) {
        return put(key, new ByteTag(value));
    }

    public AbstractTag<?> putShort(String key, short value) {
        return put(key, new ShortTag(value));
    }

    public AbstractTag<?> putInt(String key, int value) {
        return put(key, new IntegerTag(value));
    }

    public AbstractTag<?> putLong(String key, long value) {
        return put(key, new LongTag(value));
    }

    public AbstractTag<?> putFloat(String key, float value) {
        return put(key, new FloatTag(value));
    }

    public AbstractTag<?> putDouble(String key, double value) {
        return put(key, new DoubleTag(value));
    }

    public AbstractTag<?> putString(String key, String value) {
        return put(key, new StringTag(value));
    }

    public AbstractTag<?> putByteArray(String key, byte[] value) {
        return put(key, new ByteArrayTag(value));
    }

    public AbstractTag<?> putIntArray(String key, int[] value) {
        return put(key, new IntegerArrayTag(value));
    }

    public AbstractTag<?> putLongArray(String key, long[] value) {
        return put(key, new LongArrayTag(value));
    }

    public int size() {
        return getValue().size();
    }

    public boolean isEmpty() {
        return getValue().isEmpty();
    }

    public boolean containsKey(Object key) {
        return getValue().containsKey(key);
    }

    public boolean containsValue(Object value) {
        return getValue().containsValue(value);
    }

    public AbstractTag<?> get(Object key) {
        return getValue().get(key);
    }

    public <C extends AbstractTag<?>> C get(String key, Class<C> type) {
        AbstractTag<?> t = getValue().get(key);
        if (t != null) {
            return type.cast(t);
        }
        return null;
    }

    @Nullable
    public AbstractTag<?> put(String key, AbstractTag<?> value) {
        return getValue().put(key, value);
    }

    public AbstractTag<?> remove(Object key) {
        return getValue().remove(key);
    }

    public void putAll(@NotNull Map<? extends String, ? extends AbstractTag<?>> m) {
        getValue().putAll(m);
    }

    public void clear() {
        getValue().clear();
    }

    @NotNull
    public Set<String> keySet() {
        return getValue().keySet();
    }

    @NotNull
    public Collection<AbstractTag<?>> values() {
        return getValue().values();
    }

    @NotNull
    public Set<Entry<String, AbstractTag<?>>> entrySet() {
        return getValue().entrySet();
    }
}
