package com.mcreater.amclcore.exceptions.report;

import lombok.Getter;

import java.util.List;
import java.util.Vector;
import java.util.function.BiConsumer;

public class ExceptionReporter {
    public enum ExceptionType {
        UNKNOWN,
        CONCURRENT,
        REFLECT,
        NATIVE,
        IO
    }

    @Getter
    private static final List<BiConsumer<Throwable, ExceptionType>> reporters = new Vector<>();

    public static void report(Throwable throwable, ExceptionType type) {
        throwable.printStackTrace(System.out);
        reporters.parallelStream().forEach(c -> c.accept(throwable, type));
    }

    public static void report(Throwable throwable) {
        report(throwable, ExceptionType.UNKNOWN);
    }
}
