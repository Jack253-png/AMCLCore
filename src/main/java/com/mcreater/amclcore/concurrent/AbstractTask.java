package com.mcreater.amclcore.concurrent;

import java.lang.reflect.Field;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

public abstract class AbstractTask<T> extends FutureTask<T> {
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
    public Callable<T> getCallable() {
        try {
            Field field = FutureTask.class.getDeclaredField("callable");
            field.setAccessible(true);
            return (Callable<T>) field.get(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return () -> null;
    }

    /**
     * abstract task for implement
     *
     * @return the task result
     * @throws Exception when task throws an exception
     */
    public abstract T call() throws Exception;
}
