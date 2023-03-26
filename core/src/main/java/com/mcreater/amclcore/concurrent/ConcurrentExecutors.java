package com.mcreater.amclcore.concurrent;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
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
            new ThreadPoolExecutor.AbortPolicy()
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
            new ThreadPoolExecutor.AbortPolicy()
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
            new ThreadPoolExecutor.AbortPolicy()
    );
    /**
     * interface event queue
     */
    public static final Map<AbstractTask<?, ?>, ThreadPoolExecutor> INTERFACE_EVENT_EXECUTORS = new HashMap<>();

    public static ThreadPoolExecutor createInterfaceEventExecutor() {
        return new ThreadPoolExecutor(
                1,
                1,
                1,
                TimeUnit.MINUTES,
                new ArrayBlockingQueue<>(1),
                new SimpleThreadFactory(),
                new ThreadPoolExecutor.CallerRunsPolicy()
        );
    }

    /**
     * submit a task to executor
     *
     * @param executor the target executor
     * @param task     the task to be executed
     * @param <T>      the return type of task
     * @param <V>      the task state type of task
     * @return the executed future task
     */
    public static <T, V> AbstractTask<T, V> submit(ExecutorService executor, AbstractTask<T, V> task) {
        EVENT_LOGGER.info(String.format("Task %s submitted to executor %s", task, executor));
        executor.execute(task);
        return task;
    }

    /**
     * submit some tasks to executor
     * @param executor the target executor
     * @param tasks the tasks to be executed
     * @param <T> the return type of task
     * @param <V> the task state type of task
     * @return the executed future tasks
     */
    @SafeVarargs
    public static <T, V> List<AbstractTask<T, V>> submit(ExecutorService executor, AbstractTask<T, V>... tasks) {
        return Arrays.stream(tasks)
                .map(task -> submit(executor, task))
                .collect(Collectors.toList());
    }

    /**
     * submit some non-type tasks to executor
     * @param executor the target executor
     * @param tasks the tasks to be executed
     * @return the executed future tasks
     */
    public static List<AbstractTask<?, ?>> submitEx(ExecutorService executor, AbstractTask<?, ?>... tasks) {
        return Arrays.stream(tasks)
                .map(task -> submit(executor, task))
                .collect(Collectors.toList());
    }
}
