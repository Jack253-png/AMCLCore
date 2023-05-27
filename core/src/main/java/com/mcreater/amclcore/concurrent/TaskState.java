package com.mcreater.amclcore.concurrent;

import com.mcreater.amclcore.i18n.Text;
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
    @Builder.Default
    private int totalStage = 1;
    @Builder.Default
    private int currentStage = 1;
    private Text message;

    public double getStageDouble() {
        return totalStage != 0 ? (double) currentStage / totalStage : 0;
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
