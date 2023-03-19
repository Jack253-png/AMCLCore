package com.mcreater.amclcore.concurrent;

import com.mcreater.amclcore.exceptions.report.ExceptionReporter;
import lombok.Getter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;
import java.util.Vector;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.function.Consumer;

public abstract class AbstractTask<T, V> extends FutureTask<Optional<T>> {
    private static final Logger EVENT_LOGGER = LogManager.getLogger(AbstractTask.class);
    @Getter
    private final List<Consumer<T>> resultConsumers = new Vector<>();
    @Getter
    private final List<Consumer<TaskState<V>>> stateConsumers = new Vector<>();
    @Getter
    private TaskState<V> state;

    public AbstractTask() {
        super(Optional::empty);
        setCallable();
    }

    protected void setState(TaskState<V> state) {
        this.state = state;
        getStateConsumers().forEach(c -> c.accept(state));
    }

    /**
     * Set internal callable after task instanced
     */
    private void setCallable() {
        try {
            Field field = FutureTask.class.getDeclaredField("callable");
            field.setAccessible(true);
            field.set(this, (Callable<Optional<T>>) AbstractTask.this::callInternal);
        } catch (Exception e) {
            ExceptionReporter.report(e, ExceptionReporter.ExceptionType.REFLECT);
        }
    }

    /**
     * abstract task for implement
     *
     * @return the task result
     * @throws Exception when task throws an exception
     */
    public abstract T call() throws Exception;

    private Optional<T> callInternal() {
        try {
            T result = call();
            EVENT_LOGGER.info(String.format("Task %s finished", this));
            getResultConsumers().forEach(c -> c.accept(result));
            return Optional.ofNullable(result);
        } catch (Exception e) {
            ExceptionReporter.report(e, ExceptionReporter.ExceptionType.CONCURRENT);
            setState(TaskState.<V>builder()
                    .throwable(e)
                    .build()
            );
        }
        return Optional.empty();
    }
}
