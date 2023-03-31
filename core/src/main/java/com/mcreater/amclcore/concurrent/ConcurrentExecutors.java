package com.mcreater.amclcore.concurrent;

import com.mcreater.amclcore.exceptions.report.ExceptionReporter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinWorkerThread;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import static com.mcreater.amclcore.exceptions.report.ExceptionReporter.report;

public class ConcurrentExecutors {
    private static final Logger EVENT_LOGGER = LogManager.getLogger(ConcurrentExecutors.class);

    public static class SimpleThreadFactory implements ThreadFactory {
        private static final AtomicInteger poolNumber = new AtomicInteger(1);
        private final ThreadGroup group;
        private final AtomicInteger threadNumber = new AtomicInteger(1);
        private final String namePrefix;

        SimpleThreadFactory() {
            group = Thread.currentThread().getThreadGroup();
            namePrefix = "pool-" +
                    poolNumber.getAndIncrement() +
                    "-thread-";
        }

        public Thread newThread(Runnable r) {
            Thread t = new Thread(group, r,
                    namePrefix + threadNumber.getAndIncrement(),
                    0);
            if (!t.isDaemon())
                t.setDaemon(true);
            if (t.getPriority() != Thread.NORM_PRIORITY)
                t.setPriority(Thread.NORM_PRIORITY);
            return t;
        }
    }

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
    public static final ForkJoinPool EVENT_QUEUE_EXECUTOR = new ForkJoinPool(
            32,
            new ForkJoinWorkerThreadFactoryImpl(),
            (t, e) -> report(e, ExceptionReporter.ExceptionType.CONCURRENT),
            true
    );
    /**
     * OAuth login queue
     */
    public static final ForkJoinPool OAUTH_LOGIN_EXECUTOR = new ForkJoinPool(
            8,
            new ForkJoinWorkerThreadFactoryImpl(),
            (t, e) -> report(e, ExceptionReporter.ExceptionType.CONCURRENT),
            true
    );
    /**
     * Swing event queue(clipboard, desktop api...)
     */
    public static final ForkJoinPool AWT_EVENT_EXECUTOR = new ForkJoinPool(
            8,
            new ForkJoinWorkerThreadFactoryImpl(),
            (t, e) -> report(e, ExceptionReporter.ExceptionType.CONCURRENT),
            true
    );
    /**
     * interface event queue
     */
    public static final Map<AbstractTask<?, ?>, ForkJoinPool> INTERFACE_EVENT_EXECUTORS = new HashMap<>();

    public static ForkJoinPool createInterfaceEventExecutor() {
        return new ForkJoinPool(
                1,
                new ForkJoinWorkerThreadFactoryImpl(),
                (t, e) -> report(e, ExceptionReporter.ExceptionType.CONCURRENT),
                true
        );
    }
}
