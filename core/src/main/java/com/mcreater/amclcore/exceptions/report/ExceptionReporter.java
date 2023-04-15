package com.mcreater.amclcore.exceptions.report;

import com.mcreater.amclcore.concurrent.ConcurrentExecutors;
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

    static {
        ConcurrentExecutors.INTERFACE_EVENT_EXECUTORS.put(ExceptionReporter.class, ConcurrentExecutors.createInterfaceEventExecutor());
    }

    public static void report(Throwable throwable, ExceptionType type) {
        throwable.printStackTrace(System.out);
        ConcurrentExecutors.INTERFACE_EVENT_EXECUTORS.get(ExceptionReporter.class).execute(() -> getReporters().parallelStream().forEach(c -> c.accept(throwable, type)));
    }

    public static void report(Throwable throwable) {
        report(throwable, ExceptionType.UNKNOWN);
    }
}
