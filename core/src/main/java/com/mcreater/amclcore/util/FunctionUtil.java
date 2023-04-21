package com.mcreater.amclcore.util;

import org.jetbrains.annotations.NotNull;

import java.util.function.*;

public class FunctionUtil {
    public static <T> Function<T, T> genSelfFunction(@NotNull Consumer<T> consumer) {
        return Function.<T>identity().andThen(t -> {
            consumer.accept(t);
            return t;
        });
    }

    public static Function<Void, Void> toFunction(Runnable runnable) {
        return unused -> {
            runnable.run();
            return null;
        };
    }

    public static Runnable toRunnable(Function<Void, Void> function) {
        return () -> function.apply(null);
    }

    public static <T> Function<T, Void> toFunction(Consumer<T> consumer) {
        return t -> {
            consumer.accept(t);
            return null;
        };
    }

    public static Function<Integer, Void> toFunction(IntConsumer consumer) {
        return t -> {
            consumer.accept(t);
            return null;
        };
    }

    public static Function<Long, Void> toFunction(LongConsumer consumer) {
        return t -> {
            consumer.accept(t);
            return null;
        };
    }

    public static Function<Double, Void> toFunction(DoubleConsumer consumer) {
        return t -> {
            consumer.accept(t);
            return null;
        };
    }

    public static <T> Consumer<T> toConsumer(Function<T, Void> function) {
        return function::apply;
    }

    public static IntConsumer toIntConsumer(Function<Integer, Void> function) {
        return function::apply;
    }

    public static LongConsumer toLongConsumer(Function<Long, Void> function) {
        return function::apply;
    }

    public static DoubleConsumer toDoubleConsumer(Function<Double, Void> function) {
        return function::apply;
    }

    public static <T> Function<Void, T> toFunction(Supplier<T> supplier) {
        return unused -> supplier.get();
    }

    public static Function<Void, Integer> toFunction(IntSupplier supplier) {
        return unused -> supplier.getAsInt();
    }

    public static Function<Void, Long> toFunction(LongSupplier supplier) {
        return unused -> supplier.getAsLong();
    }

    public static Function<Void, Double> toFunction(DoubleSupplier supplier) {
        return unused -> supplier.getAsDouble();
    }

    public static <T> Supplier<T> toSupplier(Function<Void, T> function) {
        return () -> function.apply(null);
    }

    public static IntSupplier toIntSupplier(Function<Void, Integer> function) {
        return () -> function.apply(null);
    }

    public static LongSupplier toLongSupplier(Function<Void, Long> function) {
        return () -> function.apply(null);
    }

    public static DoubleSupplier toDoubleSupplier(Function<Void, Double> function) {
        return () -> function.apply(null);
    }
}
