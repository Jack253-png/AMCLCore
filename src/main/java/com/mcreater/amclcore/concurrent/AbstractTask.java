package com.mcreater.amclcore.concurrent;

import lombok.Getter;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.function.Consumer;

public abstract class AbstractTask<T> extends FutureTask<T> {
    @Getter
    private final List<Consumer<T>> resultConsumers = new Vector<>();

    public AbstractTask() {
        super(() -> null);
        setCallable();
    }

    /**
     * Set internal callable after task instanced
     */
    private void setCallable() {
        try {
            Field field = FutureTask.class.getDeclaredField("callable");
            field.setAccessible(true);
            field.set(this, (Callable<T>) AbstractTask.this::call);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Get internal callable
     */
    private Callable<T> getCallable() {
        try {
            Field field = FutureTask.class.getDeclaredField("callable");
            field.setAccessible(true);
            return (Callable<T>) field.get(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return () -> null;
    }

    public T callableCall() throws Exception {
        return getCallable().call();
    }

    /**
     * abstract task for implement
     *
     * @return the task result
     * @throws Exception when task throws an exception
     */
    public abstract T call() throws Exception;
}
