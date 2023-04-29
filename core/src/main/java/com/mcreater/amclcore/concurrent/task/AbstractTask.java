package com.mcreater.amclcore.concurrent.task;

import com.mcreater.amclcore.concurrent.TaskState;
import com.mcreater.amclcore.exceptions.report.ExceptionReporter;
import com.mcreater.amclcore.i18n.Text;
import com.mcreater.amclcore.util.sets.ImmutableDoubleValueSet;
import lombok.Getter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import javax.naming.OperationNotSupportedException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Vector;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;
import java.util.function.Consumer;

import static com.mcreater.amclcore.concurrent.ConcurrentExecutors.INTERFACE_EVENT_EXECUTORS;
import static com.mcreater.amclcore.concurrent.ConcurrentExecutors.createInterfaceEventExecutor;
import static com.mcreater.amclcore.i18n.I18NManager.translatable;

public abstract class AbstractTask<T> extends RecursiveTask<Optional<T>> {
    public static void printTextData(TaskState<?> taskState, Consumer<String> output) {
        Optional.ofNullable(taskState)
                .map(TaskState::getMessage)
                .map(Text::getText)
                .ifPresent(output);
    }

    private static final Logger EVENT_LOGGER = LogManager.getLogger(AbstractTask.class);
    private final List<Consumer<TaskState<T>>> stateConsumers = new Vector<>();
    private final List<Consumer<AbstractTask<?>>> bindConsumers = new Vector<>();
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

    private void addSubTask(AbstractTask<?> task) throws OperationNotSupportedException {
        if (task == this) throw new OperationNotSupportedException("task == this!");
        if (task == null) throw new NullPointerException("task == null!");
        bindTasks.add(task);
        task.topTask = this;
        INTERFACE_EVENT_EXECUTORS.get(this).execute(() -> getBindConsumers().forEach(c -> c.accept(task)));
    }

    public AbstractTask<T> bindTo(@Nullable AbstractTask<?> task) {
        fork();
        Optional.ofNullable(task).ifPresent(t -> {
            try {
                t.addSubTask(AbstractTask.this);
            } catch (Exception e) {
                ExceptionReporter.report(e, ExceptionReporter.ExceptionType.UNKNOWN);
            }
        });
        return this;
    }

    public AbstractTask<T> bind() {
        return bindTo(null);
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

    public AbstractTask<T> submitTo(ForkJoinPool pool) {
        pool.execute(this);
        return this;
    }
}