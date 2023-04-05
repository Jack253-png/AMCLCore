package com.mcreater.amclcore.concurrent;

import com.mcreater.amclcore.i18n.I18NManager;
import lombok.Builder;
import lombok.Data;

import java.util.function.Consumer;

@Data
@Builder
public class TaskState<R> {
    @Builder.Default
    private Type taskType = Type.EXECUTING;
    private Throwable throwable;
    private R result;
    private int totalStage;
    private int currentStage;
    private I18NManager.Text message;

    public double getStageDouble() {
        return (double) currentStage / totalStage;
    }

    public int getStateInt() {
        return (int) (getStageDouble() * 100);
    }

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

    public boolean isFinish() {
        return taskType == Type.FINISHED;
    }

    public void executeIfFinish(Consumer<R> c) {
        if (isFinish() && result != null) c.accept(result);
    }
}
