package com.mcreater.amclcore.concurrent;

import java.lang.reflect.Field;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

public abstract class AbstractTask<T> extends FutureTask<T> {
    public AbstractTask() {
        super(() -> null);
        setCallable();
    }

    private void setCallable() {
        try {
            Field field = FutureTask.class.getDeclaredField("callable");
            field.setAccessible(true);
            field.set(this, (Callable<T>) AbstractTask.this::call);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public abstract T call() throws Exception;
}
