package com.mcreater.amclcore.concurrent;

import com.mcreater.amclcore.concurrent.task.AbstractTask;
import lombok.Getter;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Vector;
import java.util.concurrent.Callable;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.function.Consumer;

public class ExtendForkJoinPool extends ForkJoinPool {
    private final List<AbstractTask<?>> tasks = new Vector<>();
    private final List<ForkJoinTask<?>> allTasks = new Vector<>();
    @Getter
    private final List<Consumer<ForkJoinTask<?>>> baseListeners = new Vector<>();
    @Getter
    private final List<Consumer<AbstractTask<?>>> wrappedListeners = new Vector<>();
    private final ForkJoinPool eventPool = ConcurrentExecutors.createInterfaceEventExecutor();

    public ExtendForkJoinPool(int parallelism,
                              ForkJoinWorkerThreadFactory factory,
                              Thread.UncaughtExceptionHandler handler,
                              boolean asyncMode) {
        super(parallelism, factory, handler, asyncMode);
    }

    public List<AbstractTask<?>> getTasks() {
        return Collections.unmodifiableList(tasks);
    }

    public List<ForkJoinTask<?>> getAllTasks() {
        return Collections.unmodifiableList(allTasks);
    }

    private <T> ForkJoinTask<T> processTask(ForkJoinTask<T> task) {
        allTasks.add(task);
        if (task instanceof AbstractTask<?>) tasks.add((AbstractTask<?>) task);
        eventPool.execute(() -> {
            baseListeners.forEach(t -> t.accept(task));
            if (task instanceof AbstractTask<?>) wrappedListeners.forEach(t -> t.accept((AbstractTask<?>) task));
        });
        return task;
    }

    public ForkJoinTask<?> submit(Runnable task) {
        return Optional.of(super.submit(task)).map(this::processTask).get();
    }

    public <T> ForkJoinTask<T> submit(Callable<T> task) {
        return Optional.of(super.submit(task)).map(this::processTask).get();
    }

    public <T> ForkJoinTask<T> submit(ForkJoinTask<T> task) {
        return Optional.of(super.submit(task)).map(this::processTask).get();
    }

    public <T> ForkJoinTask<T> submit(Runnable task, T result) {
        return Optional.of(super.submit(task, result)).map(this::processTask).get();
    }

    public void execute(Runnable task) {
        processTask(super.submit(task));
    }

    public void execute(ForkJoinTask<?> task) {
        processTask(super.submit(task));
    }
}
