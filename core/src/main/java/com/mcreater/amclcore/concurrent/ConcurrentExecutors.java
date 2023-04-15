package com.mcreater.amclcore.concurrent;

import com.mcreater.amclcore.exceptions.report.ExceptionReporter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinWorkerThread;

import static com.mcreater.amclcore.exceptions.report.ExceptionReporter.report;

public class ConcurrentExecutors {
    private static final Logger EVENT_LOGGER = LogManager.getLogger(ConcurrentExecutors.class);

    public static class ForkJoinWorkerThreadFactoryImpl implements ForkJoinPool.ForkJoinWorkerThreadFactory {

        public ForkJoinWorkerThread newThread(ForkJoinPool pool) {
            ForkJoinWorkerThread t = ForkJoinPool.defaultForkJoinWorkerThreadFactory.newThread(pool);
            t.setDaemon(true);
            return t;
        }
    }

    /**
     * Main event queue(login, launch, download, task shells...)
     */
    public static final ExtendForkJoinPool EVENT_QUEUE_EXECUTOR = new ExtendForkJoinPool(
            32,
            new ForkJoinWorkerThreadFactoryImpl(),
            (t, e) -> report(e, ExceptionReporter.ExceptionType.CONCURRENT),
            true
    );
    /**
     * OAuth login queue
     */
    public static final ExtendForkJoinPool OAUTH_LOGIN_EXECUTOR = new ExtendForkJoinPool(
            8,
            new ForkJoinWorkerThreadFactoryImpl(),
            (t, e) -> report(e, ExceptionReporter.ExceptionType.CONCURRENT),
            true
    );
    /**
     * Swing event queue(clipboard, desktop api...)
     */
    public static final ExtendForkJoinPool AWT_EVENT_EXECUTOR = new ExtendForkJoinPool(
            8,
            new ForkJoinWorkerThreadFactoryImpl(),
            (t, e) -> report(e, ExceptionReporter.ExceptionType.CONCURRENT),
            true
    );
    /**
     * interface event queue
     */
    public static final Map<Object, ForkJoinPool> INTERFACE_EVENT_EXECUTORS = new HashMap<>();

    public static ForkJoinPool createInterfaceEventExecutor() {
        return new ForkJoinPool(
                1,
                new ForkJoinWorkerThreadFactoryImpl(),
                (t, e) -> report(e, ExceptionReporter.ExceptionType.CONCURRENT),
                true
        );
    }
}
