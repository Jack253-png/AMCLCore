package com.mcreater.amclcore.concurrent;

import com.mcreater.amclcore.exceptions.report.ExceptionReporter;
import lombok.Getter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Optional;
import java.util.Vector;
import java.util.concurrent.RecursiveTask;
import java.util.function.Consumer;

import static com.mcreater.amclcore.concurrent.ConcurrentExecutors.INTERFACE_EVENT_EXECUTORS;
import static com.mcreater.amclcore.concurrent.ConcurrentExecutors.createInterfaceEventExecutor;

public abstract class AbstractTask<T, V> extends RecursiveTask<Optional<T>> {
    private static final Logger EVENT_LOGGER = LogManager.getLogger(AbstractTask.class);
    @Getter
    private final List<Consumer<TaskState<V, T>>> stateConsumers = new Vector<>();
    @Getter
    private TaskState<V, T> state;

    public AbstractTask<T, V> addStateConsumers(List<Consumer<TaskState<V, T>>> c) {
        c.forEach(this::addStateConsumer);
        return this;
    }

    public AbstractTask<T, V> addStateConsumer(Consumer<TaskState<V, T>> c) {
        stateConsumers.add(c);
        return this;
    }

    public AbstractTask() {
        super();
        INTERFACE_EVENT_EXECUTORS.put(this, createInterfaceEventExecutor());
    }

    protected void setState(TaskState<V, T> state) {
        this.state = state;
        INTERFACE_EVENT_EXECUTORS.get(this).execute(() -> getStateConsumers().forEach(c -> c.accept(state)));
    }

    /**
     * abstract task for implement
     *
     * @return the task result
     * @throws Exception when task throws an exception
     */
    public abstract T call() throws Exception;

    protected Optional<T> compute() {
        try {
            T result = call();
            EVENT_LOGGER.info(String.format("Task %s finished", this));
            setState(TaskState.<V, T>builder()
                    .taskType(TaskState.Type.FINISHED)
                    .result(result)
                    .build()
            );
            return Optional.ofNullable(result);
        } catch (Exception e) {
            ExceptionReporter.report(e, ExceptionReporter.ExceptionType.CONCURRENT);
            setState(TaskState.<V, T>builder()
                    .throwable(e)
                    .taskType(TaskState.Type.ERROR)
                    .build()
            );
        }
        return Optional.empty();
    }

    public static <T, V> TaskState<T, V> createTaskState(T value) {
        return TaskState.<T, V>builder()
                .data(value)
                .build();
    }
}
