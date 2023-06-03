package com.mcreater.amclcore.nbtlib.common.tags;

import org.jetbrains.annotations.NotNull;

import java.util.*;

public class ListTag<T extends AbstractTag<?>> extends AbstractTag<List<T>> implements Iterable<T>, Comparable<ListTag<T>>, List<T> {
    private Class<? super T> typeClass;

    public ListTag(Class<? super T> clazz, List<T> value) {
        super(value);
        typeClass = clazz;
    }

    private ListTag(int initialCapacity) {
        super(createEmptyValue(initialCapacity));
    }

    /**
     * <p>Creates an empty mutable list to be used as empty value of ListTags.</p>
     *
     * @param <T>             Type of the list elements
     * @param initialCapacity The initial capacity of the returned List
     * @return An instance of {@link java.util.List} with an initial capacity of 3
     */
    private static <T> List<T> createEmptyValue(int initialCapacity) {
        return new ArrayList<>(initialCapacity);
    }

    /**
     * @param typeClass The exact class of the elements
     * @throws IllegalArgumentException When {@code typeClass} is {@link EndTag}{@code .class}
     * @throws NullPointerException     When {@code typeClass} is {@code null}
     */
    public ListTag(Class<? super T> typeClass) throws IllegalArgumentException, NullPointerException {
        this(typeClass, 3);
    }

    /**
     * @param typeClass       The exact class of the elements
     * @param initialCapacity Initial capacity of list
     * @throws IllegalArgumentException When {@code typeClass} is {@link EndTag}{@code .class}
     * @throws NullPointerException     When {@code typeClass} is {@code null}
     */
    public ListTag(Class<? super T> typeClass, int initialCapacity) throws IllegalArgumentException, NullPointerException {
        super(createEmptyValue(initialCapacity));
        /*if (typeClass == EndTag.class) {
            throw new IllegalArgumentException("cannot create ListTag with EndTag elements");
        }*/
        this.typeClass = Objects.requireNonNull(typeClass);
    }

    public ListTag<T> clone() {
        return new ListTag<T>(typeClass, new Vector<>(getValue()));
    }

    public int compareTo(@NotNull ListTag<T> o) {
        return getValue().hashCode() - o.getValue().hashCode();
    }

    public int size() {
        return getValue().size();
    }

    public boolean isEmpty() {
        return getValue().isEmpty();
    }

    public boolean contains(Object o) {
        return getValue().contains(o);
    }

    @NotNull
    public Object @NotNull [] toArray() {
        return getValue().toArray();
    }

    @NotNull
    public <T1> T1 @NotNull [] toArray(@NotNull T1[] a) {
        return getValue().toArray(a);
    }

    public boolean add(T t) {
        return getValue().add(t);
    }

    public boolean remove(Object o) {
        return getValue().remove(o);
    }

    public boolean containsAll(@NotNull Collection<?> c) {
        return new HashSet<>(getValue()).containsAll(c);
    }

    public boolean addAll(@NotNull Collection<? extends T> c) {
        return getValue().addAll(c);
    }

    public boolean addAll(int index, @NotNull Collection<? extends T> c) {
        return getValue().addAll(index, c);
    }

    public boolean removeAll(@NotNull Collection<?> c) {
        return getValue().removeAll(c);
    }

    public boolean retainAll(@NotNull Collection<?> c) {
        return getValue().retainAll(c);
    }

    public void clear() {
        getValue().clear();
    }

    public T get(int index) {
        return getValue().get(index);
    }

    public T set(int index, T element) {
        return getValue().set(index, element);
    }

    public void add(int index, T element) {
        getValue().add(index, element);
    }

    public T remove(int index) {
        return getValue().remove(index);
    }

    public int indexOf(Object o) {
        return getValue().indexOf(o);
    }

    public int lastIndexOf(Object o) {
        return getValue().lastIndexOf(o);
    }

    @NotNull
    public ListIterator<T> listIterator() {
        return getValue().listIterator();
    }

    @NotNull
    public ListIterator<T> listIterator(int index) {
        return getValue().listIterator(index);
    }

    @NotNull
    public List<T> subList(int fromIndex, int toIndex) {
        return getValue().subList(fromIndex, toIndex);
    }

    @NotNull
    public Iterator<T> iterator() {
        return getValue().iterator();
    }
}
