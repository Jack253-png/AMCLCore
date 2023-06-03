package com.mcreater.amclcore.nbtlib.common.tags;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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
