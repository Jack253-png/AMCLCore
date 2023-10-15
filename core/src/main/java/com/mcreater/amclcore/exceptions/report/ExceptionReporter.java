package com.mcreater.amclcore.exceptions.report;

import com.mcreater.amclcore.concurrent.ConcurrentExecutors;
import lombok.Getter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Vector;
import java.util.function.BiConsumer;

import static com.mcreater.amclcore.i18n.I18NManager.translatable;

public class ExceptionReporter {
    private static final Logger EVENT_LOGGER = LogManager.getLogger(ExceptionReporter.class);

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
        ConcurrentExecutors.getINTERFACE_EVENT_EXECUTORS().put(ExceptionReporter.class, ConcurrentExecutors.createInterfaceEventExecutor());
    }

    public static void report(Throwable throwable, ExceptionType type) {
        EVENT_LOGGER.error(translatable("core.exception.reporting").getText());
        throwable.printStackTrace();
        ConcurrentExecutors.getINTERFACE_EVENT_EXECUTORS().get(ExceptionReporter.class).execute(() -> getReporters().parallelStream().forEach(c -> c.accept(throwable, type)));
    }

    public static void report(Throwable throwable) {
        report(throwable, ExceptionType.UNKNOWN);
    }
}
