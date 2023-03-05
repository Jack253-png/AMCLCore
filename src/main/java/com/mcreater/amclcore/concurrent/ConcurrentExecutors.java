package com.mcreater.amclcore.concurrent;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class ConcurrentExecutors {
    /**
     * Main event queue(login, launch, download, task shells...)
     */
    public static final ThreadPoolExecutor EVENT_QUEUE_EXECUTOR = new ThreadPoolExecutor(
            32,
            64,
            1,
            TimeUnit.MINUTES,
            new ArrayBlockingQueue<>(64),
            Executors.defaultThreadFactory(),
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
            Executors.defaultThreadFactory(),
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
            Executors.defaultThreadFactory(),
            new ThreadPoolExecutor.DiscardOldestPolicy()
    );

    /**
     * Fastly run tasks
     *
     * @param executor the thread executor
     * @param tasks    tasks to be executed
     * @return task result
     */
    public static List<? extends Object> runAllTask(ExecutorService executor, AbstractTask<?>... tasks) {
        return Arrays.stream(tasks)
                .map(tAbstractTask -> executor.submit(tAbstractTask::call))
                .map(ConcurrentExecutors::getFuture)
                .collect(Collectors.toList());
    }

    private static <T> T getFuture(Future<T> tFuture) {
        try {
            return tFuture.get();
        } catch (Exception ignored) {
        }
        return null;
    }

    /**
     * convert task to callable
     *
     * @param task task to be converted
     * @param <T>  the task result type
     * @return converted callable
     */
    public static <T> Callable<T> fromTask(AbstractTask<T> task) {
        return task::call;
    }
}
