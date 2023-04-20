package com.mcreater.amclcore.concurrent;

import com.mcreater.amclcore.exceptions.report.ExceptionReporter;
import lombok.Getter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.naming.OperationNotSupportedException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Vector;
import java.util.concurrent.RecursiveTask;
import java.util.function.Consumer;

import static com.mcreater.amclcore.concurrent.ConcurrentExecutors.INTERFACE_EVENT_EXECUTORS;
import static com.mcreater.amclcore.concurrent.ConcurrentExecutors.createInterfaceEventExecutor;

public abstract class AbstractTask<T> extends RecursiveTask<Optional<T>> {
    private static final Logger EVENT_LOGGER = LogManager.getLogger(AbstractTask.class);
    @Getter
    private final List<Consumer<TaskState<T>>> stateConsumers = new Vector<>();
    @Getter
    private TaskState<T> state;
    private final List<AbstractTask<?>> bindTasks = new Vector<>();
    @Getter
    private AbstractTask<?> topTask;

    public AbstractTask<T> addStateConsumers(List<Consumer<TaskState<T>>> c) {
        c.forEach(this::addStateConsumer);
        return this;
    }

    public AbstractTask<T> addStateConsumer(Consumer<TaskState<T>> c) {
        stateConsumers.add(c);
        return this;
    }

    public AbstractTask() {
        super();
        INTERFACE_EVENT_EXECUTORS.put(this, createInterfaceEventExecutor());
    }

    protected void setState(TaskState<T> state) {
        this.state = state;
        INTERFACE_EVENT_EXECUTORS.get(this).execute(() -> getStateConsumers().forEach(c -> c.accept(state)));
    }

    protected void setTopTaskState(TaskState s) {
        Optional.ofNullable(topTask).ifPresent(abstractTask -> abstractTask.setState(s));
    }

    /**
     * abstract task for implement<br>
     * 用于实现的抽象任务
     *
     * @return the task result<br>任务结果
     * @throws Exception when task throws an exception<br>当任务抛出一个异常
     */
    protected abstract T call() throws Exception;

    protected Optional<T> compute() {
        int lastTotal = Optional.ofNullable(state).map(TaskState::getTotalStage).orElse(1);
        int lastCurr = Optional.ofNullable(state).map(TaskState::getCurrentStage).orElse(1);
        try {
            T result = call();
            EVENT_LOGGER.info(String.format("Task %s finished", this));
            setState(TaskState.<T>builder()
                    .taskType(TaskState.Type.FINISHED)
                    .totalStage(lastTotal)
                    .currentStage(lastTotal)
                    .result(result)
                    .build()
            );
            return Optional.ofNullable(result);
        } catch (Exception e) {
            ExceptionReporter.report(e, ExceptionReporter.ExceptionType.CONCURRENT);
            setState(TaskState.<T>builder()
                    .throwable(e)
                    .totalStage(lastTotal)
                    .currentStage(lastCurr)
                    .taskType(TaskState.Type.ERROR)
                    .build()
            );
        }
        return Optional.empty();
    }

    private void bindTask(AbstractTask<?> task) throws OperationNotSupportedException {
        Optional.ofNullable(task.topTask).ifPresent(task1 -> {
            task1.bindTasks.remove(task);
            task.topTask = null;
        });
        if (this.topTask != null) throw new OperationNotSupportedException("this.topTask != null");
        bindTasks.add(task);
        task.topTask = this;
    }

    public AbstractTask<T> bindTo(AbstractTask<?> task) {
        try {
            task.bindTask(this);
        } catch (Exception e) {
            ExceptionReporter.report(e, ExceptionReporter.ExceptionType.UNKNOWN);
        }
        return this;
    }

    public List<AbstractTask<?>> getSubTasks() {
        return Collections.unmodifiableList(bindTasks);
    }
}
