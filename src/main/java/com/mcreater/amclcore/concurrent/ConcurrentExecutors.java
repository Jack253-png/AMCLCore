package com.mcreater.amclcore.concurrent;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class ConcurrentExecutors {
    private static final Logger EVENT_LOGGER = LogManager.getLogger(ConcurrentExecutors.class);

    public static class SimpleThreadFactory implements ThreadFactory {
        private static final AtomicInteger poolNumber = new AtomicInteger(1);
        private final ThreadGroup group;
        private final AtomicInteger threadNumber = new AtomicInteger(1);
        private final String namePrefix;

        SimpleThreadFactory() {
            SecurityManager s = System.getSecurityManager();
            group = (s != null) ? s.getThreadGroup() :
                    Thread.currentThread().getThreadGroup();
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

    /**
     * Main event queue(login, launch, download, task shells...)
     */
    public static final ThreadPoolExecutor EVENT_QUEUE_EXECUTOR = new ThreadPoolExecutor(
            32,
            64,
            1,
            TimeUnit.MINUTES,
            new ArrayBlockingQueue<>(64),
            new SimpleThreadFactory(),
            new ThreadPoolExecutor.DiscardOldestPolicy()
    );
    /**
     * OAuth login queue
     */
    public static final ThreadPoolExecutor OAUTH_LOGIN_EXECUTOR = new ThreadPoolExecutor(
            8,
            16,
            30,
            TimeUnit.SECONDS,
            new ArrayBlockingQueue<>(64),
            new SimpleThreadFactory(),
            new ThreadPoolExecutor.DiscardOldestPolicy()
    );
    /**
     * Swing event queue(clipboard, desktop api...)
     */
    public static final ThreadPoolExecutor AWT_EVENT_EXECUTOR = new ThreadPoolExecutor(
            32,
            64,
            30,
            TimeUnit.SECONDS,
            new ArrayBlockingQueue<>(64),
            new SimpleThreadFactory(),
            new ThreadPoolExecutor.DiscardOldestPolicy()
    );

    /**
     * submit a task to executor
     * @return the executed future task
     */
    public static <T> Future<T> fastSubmit(ExecutorService executor, AbstractTask<T> task) {
        EVENT_LOGGER.info(String.format("Task %s submitted to executor %s", task, executor));
        return executor.submit(() -> {
            T result = task.callableCall();
            EVENT_LOGGER.info(String.format("Task %s finished", task));
            task.getResultConsumers().forEach(tConsumer -> tConsumer.accept(result));
            return result;
        });
    }

    /**
     * submit some tasks to executor
     *
     * @return the executed future tasks
     */
    @SafeVarargs
    public static <T> List<Future<T>> fastSubmit(ExecutorService executor, AbstractTask<T>... tasks) {
        return Arrays.stream(tasks)
                .map(task -> fastSubmit(executor, task))
                .collect(Collectors.toList());
    }

    /**
     * submit some non-type tasks to executor
     *
     * @return the executed future tasks
     */
    public static List<Future<?>> fastSubmitEx(ExecutorService executor, AbstractTask<?>... tasks) {
        return Arrays.stream(tasks)
                .map(task -> fastSubmit(executor, task))
                .collect(Collectors.toList());
    }
}
