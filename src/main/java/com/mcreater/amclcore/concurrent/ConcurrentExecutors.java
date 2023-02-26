package com.mcreater.amclcore.concurrent;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class ConcurrentExecutors {
    public static final ThreadPoolExecutor EVENT_QUEUE_EXECUTOR = new ThreadPoolExecutor(
            32,
            64,
            1,
            TimeUnit.MINUTES,
            new ArrayBlockingQueue<>(64),
            Executors.defaultThreadFactory(),
            new ThreadPoolExecutor.DiscardOldestPolicy()
    );
    public static final ThreadPoolExecutor OAUTH_LOGIN_EXECUTOR = new ThreadPoolExecutor(
            8,
            16,
            30,
            TimeUnit.SECONDS,
            new ArrayBlockingQueue<>(64),
            Executors.defaultThreadFactory(),
            new ThreadPoolExecutor.AbortPolicy()
    );
    public static final ThreadPoolExecutor AWT_EVENT_EXECUTOR = new ThreadPoolExecutor(
            32,
            64,
            30,
            TimeUnit.SECONDS,
            new ArrayBlockingQueue<>(64),
            Executors.defaultThreadFactory(),
            new ThreadPoolExecutor.DiscardOldestPolicy()
    );

    public static List<? extends Object> runAllTask(ExecutorService executor, AbstractTask<?>... tasks) {
        return Arrays.stream(tasks)
                .map(tAbstractTask -> executor.submit(tAbstractTask::call))
                .map(ConcurrentExecutors::getFuture)
                .collect(Collectors.toList());
    }

    private static <T> T getFuture(Future<T> tFuture) {
        try {
            return tFuture.get();
        }
        catch (Exception ignored) {}
        return null;
    }
}
