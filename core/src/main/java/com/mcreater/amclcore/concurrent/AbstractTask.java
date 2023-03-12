package com.mcreater.amclcore.concurrent;

import lombok.Getter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.function.Consumer;

public abstract class AbstractTask<T, V> extends FutureTask<T> {
    private static final Logger EVENT_LOGGER = LogManager.getLogger(AbstractTask.class);
    @Getter
    private final List<Consumer<T>> resultConsumers = new Vector<>();
    @Getter
    private final List<Consumer<Exception>> errorConsumers = new Vector<>();
    @Getter
    private final List<Consumer<V>> stateConsumers = new Vector<>();
    @Getter
    private V state;

    public AbstractTask() {
        super(() -> null);
        setCallable();
    }

    protected void setState(V state) {
        this.state = state;
        stateConsumers.forEach(c -> c.accept(state));
    }

    public final AbstractTask<T, V> addResultHandler(Consumer<T> result) {
        getResultConsumers().add(result);
        return this;
    }

    @SafeVarargs
    public final AbstractTask<T, V> addResultHandlers(Consumer<T>... error) {
        Arrays.asList(error).forEach(this::addResultHandler);
        return this;
    }

    public final AbstractTask<T, V> addErrorHandler(Consumer<Exception> error) {
        getErrorConsumers().add(error);
        return this;
    }

    @SafeVarargs
    public final AbstractTask<T, V> addErrorHandlers(Consumer<Exception>... result) {
        Arrays.asList(result).forEach(this::addErrorHandler);
        return this;
    }

    public final AbstractTask<T, V> addStateHandler(Consumer<V> state) {
        getStateConsumers().add(state);
        return this;
    }

    @SafeVarargs
    public final AbstractTask<T, V> addStateHandlers(Consumer<V>... state) {
        Arrays.asList(state).forEach(this::addStateHandler);
        return this;
    }

    /**
     * Set internal callable after task instanced
     */
    private void setCallable() {
        try {
            Field field = FutureTask.class.getDeclaredField("callable");
            field.setAccessible(true);
            field.set(this, (Callable<T>) AbstractTask.this::callInternal);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * abstract task for implement
     *
     * @return the task result
     * @throws Exception when task throws an exception
     */
    public abstract T call() throws Exception;

    private T callInternal() {
        try {
            T result = call();
            EVENT_LOGGER.info(String.format("Task %s finished", this));
            getResultConsumers().forEach(c -> c.accept(result));
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            getErrorConsumers().forEach(tConsumer -> tConsumer.accept(e));
        }
        return null;
    }
}
