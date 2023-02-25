package com.mcreater.amclcore.concurrent;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

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
}
