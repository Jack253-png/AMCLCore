package com.mcreater.amclcore.concurrent;

import lombok.Builder;
import lombok.Data;

import java.util.function.Consumer;

@Data
@Builder
public class TaskState<T, R> {
    @Builder.Default
    private Type taskType = Type.EXECUTING;
    private Throwable throwable;
    private R result;
    private T data;

    public enum Type {
        ERROR,
        EXECUTING,
        FINISHED
    }

    public boolean isExc() {
        return taskType == Type.ERROR;
    }

    public void executeIfExc(Consumer<Throwable> c) {
        if (isExc() && throwable != null) c.accept(throwable);
    }

    public boolean isExec() {
        return taskType == Type.EXECUTING;
    }

    public void executeIfExec(Consumer<T> c) {
        if (isExec() && data != null) c.accept(data);
    }

    public boolean isFinish() {
        return taskType == Type.FINISHED;
    }

    public void executeIfFinish(Consumer<R> c) {
        if (isFinish() && result != null) c.accept(result);
    }
}
