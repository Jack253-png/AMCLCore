package com.mcreater.amclcore.concurrent;

import com.mcreater.amclcore.exceptions.report.ExceptionReporter;
import com.mcreater.amclcore.i18n.Text;
import com.mcreater.amclcore.util.sets.ImmutableDoubleValueSet;
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
import static com.mcreater.amclcore.i18n.I18NManager.translatable;

public abstract class AbstractTask<T> extends RecursiveTask<Optional<T>> {
    private static final Logger EVENT_LOGGER = LogManager.getLogger(AbstractTask.class);
    private final List<Consumer<TaskState<T>>> stateConsumers = new Vector<>();
    private final List<Consumer<AbstractTask<?>>> bindConsumers = new Vector<>();
    @Getter
    private TaskState<T> state;
    private final List<AbstractTask<?>> bindTasks = new Vector<>();
    @Getter
    private AbstractTask<?> topTask;
    protected boolean canBind = true;

    public AbstractTask<T> addStateConsumers(List<Consumer<TaskState<T>>> c) {
        c.forEach(this::addStateConsumer);
        return this;
    }

    public AbstractTask<T> addStateConsumer(Consumer<TaskState<T>> c) {
        stateConsumers.add(c);
        return this;
    }

    public AbstractTask<T> addBindConsumers(List<Consumer<AbstractTask<?>>> c) {
        c.forEach(this::addBindConsumer);
        return this;
    }

    public AbstractTask<T> addBindConsumer(Consumer<AbstractTask<?>> c) {
        bindConsumers.add(c);
        return this;
    }

    public List<Consumer<TaskState<T>>> getStateConsumers() {
        return Collections.unmodifiableList(stateConsumers);
    }

    public List<Consumer<AbstractTask<?>>> getBindConsumers() {
        return Collections.unmodifiableList(bindConsumers);
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

    private void stateFinish(ImmutableDoubleValueSet<Integer, Integer> lastState, T result) {
        setState(TaskState.<T>builder()
                .taskType(TaskState.Type.FINISHED)
                .totalStage(lastState.getValue1())
                .currentStage(lastState.getValue1())
                .result(result)
                .build()
        );
    }

    private void stateExc(ImmutableDoubleValueSet<Integer, Integer> lastState, Throwable e) {
        setState(TaskState.<T>builder()
                .message(translatable("core.concurrent.base.event.exception.text", e))
                .throwable(e)
                .totalStage(lastState.getValue1())
                .currentStage(lastState.getValue2())
                .taskType(TaskState.Type.ERROR)
                .build()
        );
    }

    private ImmutableDoubleValueSet<Integer, Integer> fetchTaskState() {
        return ImmutableDoubleValueSet.<Integer, Integer>builder()
                .value1(Optional.ofNullable(state).map(TaskState::getTotalStage).orElse(1))
                .value2(Optional.ofNullable(state).map(TaskState::getCurrentStage).orElse(1))
                .build();
    }

    protected Optional<T> compute() {
        ImmutableDoubleValueSet<Integer, Integer> lastState = fetchTaskState();
        try {
            T result = call();
            EVENT_LOGGER.info(translatable("core.concurrent.base.event.finish.name", this).getText());
            stateFinish(lastState, result);
            return Optional.ofNullable(result);
        } catch (Exception e) {
            ExceptionReporter.report(e, ExceptionReporter.ExceptionType.CONCURRENT);
            stateExc(lastState, e);
        }
        return Optional.empty();
    }

    private void bindTask(AbstractTask<?> task) throws OperationNotSupportedException {
        if (!task.canBind) throw new OperationNotSupportedException("this.canBind == false!");
        if (task.topTask != null) throw new OperationNotSupportedException("task.topTask != null!");
        if (this.topTask != null) throw new OperationNotSupportedException("this.topTask != null!");
        bindTasks.add(task);
        task.topTask = this;
        INTERFACE_EVENT_EXECUTORS.get(this).execute(() -> getBindConsumers().forEach(c -> c.accept(task)));
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

    protected abstract Text getTaskName();

    public String toString() {
        return getTaskName().getText();
    }

    public boolean tryUnfork() {
        throw new RuntimeException(new OperationNotSupportedException());
    }

    public void complete(Optional<T> value) {
        super.complete(value);
        stateFinish(fetchTaskState(), value.orElse(null));
    }

    public void completeExceptionally(Throwable ex) {
        super.completeExceptionally(ex);
        stateExc(fetchTaskState(), ex);
    }
}
