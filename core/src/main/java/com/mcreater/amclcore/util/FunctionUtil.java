package com.mcreater.amclcore.util;

import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;
import java.util.function.Function;

public class FunctionUtil {
    public static <T> Function<T, T> genSelfFunction(@NotNull Consumer<T> consumer) {
        return Function.<T>identity().andThen(t -> {
            consumer.accept(t);
            return t;
        });
    }
}
