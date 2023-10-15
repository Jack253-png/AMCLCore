package com.mcreater.amclcore.concurrent.task

import com.mcreater.amclcore.concurrent.ConcurrentExecutors
import com.mcreater.amclcore.concurrent.TaskState
import com.mcreater.amclcore.exceptions.report.ExceptionReporter
import com.mcreater.amclcore.i18n.I18NManager.Companion.translatable
import com.mcreater.amclcore.i18n.Text
import org.apache.commons.lang3.tuple.ImmutablePair
import org.apache.logging.log4j.LogManager
import java.util.*
import java.util.concurrent.ForkJoinPool
import java.util.concurrent.RecursiveTask
import java.util.function.Consumer
import javax.naming.OperationNotSupportedException


abstract class AbstractTask<T : Any?> : RecursiveTask<Optional<T>>() {
    val stateConsumers: MutableList<Consumer<TaskState<T>>> = Vector()
    val bindConsumers: MutableList<Consumer<AbstractTask<out Any?>>> = Vector()

    private var state: TaskState<T>? = null
    private val bindTasks: MutableList<AbstractTask<out Any?>> = Vector()
    protected var topTask: AbstractTask<*>? = null
    fun getState(): TaskState<T>? {
        return state
    }

    fun addStateConsumers(c: List<Consumer<TaskState<T>>>): AbstractTask<T> {
        c.forEach { addStateConsumer(it) }
        return this
    }

    fun addStateConsumer(c: Consumer<TaskState<T>>): AbstractTask<T> {
        stateConsumers.add(c)
        return this
    }

    fun addBindConsumers(c: List<Consumer<AbstractTask<out Any?>>>): AbstractTask<T> {
        c.forEach { addBindConsumer(it) }
        return this
    }

    fun addBindConsumer(c: Consumer<AbstractTask<out Any?>>): AbstractTask<T> {
        bindConsumers.add(c)
        return this
    }

    init {
        ConcurrentExecutors.INTERFACE_EVENT_EXECUTORS[this] = ConcurrentExecutors.createInterfaceEventExecutor()
    }

    protected fun setState(state: TaskState<T>) {
        this.state = state
        Optional.ofNullable(ConcurrentExecutors.INTERFACE_EVENT_EXECUTORS[this]).ifPresent { a: ForkJoinPool ->
            a.execute {
                stateConsumers.forEach(Consumer { c: Consumer<TaskState<T>> ->
                    c.accept(
                        state
                    )
                })
            }
        }
    }

    /**
     * abstract task for implement<br></br>
     * 用于实现的抽象任务
     *
     * @return the task result<br></br>任务结果
     * @throws Exception when task throws an exception<br></br>当任务抛出一个异常
     */
    @Throws(Exception::class)
    protected abstract fun call(): T
    private fun stateFinish(lastState: ImmutablePair<Int, Int>, result: T?) {
        setState(
            TaskState(
                TaskState.Type.FINISHED,
                null,
                result,
                lastState.key, lastState.key, null
            )
        )
    }

    private fun stateExc(lastState: ImmutablePair<Int, Int>, e: Throwable) {
        setState(
            TaskState(
                TaskState.Type.ERROR,
                e,
                null,
                lastState.key, lastState.value, translatable(
                    "core.concurrent.base.event.exception.text", e.javaClass.getName(),
                    getTaskName().text!!
                )
            )
        )
    }

    private fun fetchTaskState(): ImmutablePair<Int, Int> {
        return ImmutablePair(
            Optional.ofNullable(state).map { it.totalStage }.orElse(1),
            Optional.ofNullable(state).map { it.currentStage }.orElse(1)
        )
    }

    override fun compute(): Optional<T> {
        val lastState = fetchTaskState()
        try {
            val result = call()
            if ("" != this.toString()) EVENT_LOGGER.info(
                translatable(
                    "core.concurrent.base.event.finish.name",
                    this
                ).text
            )
            stateFinish(lastState, result)
            return Optional.ofNullable(result) as Optional<T>
        } catch (e: Exception) {
            ExceptionReporter.report(e, ExceptionReporter.ExceptionType.CONCURRENT)
            stateExc(lastState, e)
        }
        return Optional.empty<T>() as Optional<T>
    }

    @Throws(OperationNotSupportedException::class)
    private fun addSubTask(task: AbstractTask<out Any?>) {
        if (task === this) throw OperationNotSupportedException("task == this!")
        bindTasks.add(task)
        task.topTask = this
        ConcurrentExecutors.INTERFACE_EVENT_EXECUTORS[this]!!.execute {
            bindConsumers.forEach { it.accept(task) }
        }
    }

    fun bindTo(task: AbstractTask<*>?): AbstractTask<T> {
        fork()
        Optional.ofNullable(task).ifPresent {
            try {
                it.addSubTask(this@AbstractTask)
            } catch (e: Exception) {
                ExceptionReporter.report(e, ExceptionReporter.ExceptionType.UNKNOWN)
            }
        }
        return this
    }

    fun bind(): AbstractTask<T> {
        return bindTo(null)
    }

    val subTasks: List<AbstractTask<*>>
        get() = Collections.unmodifiableList(bindTasks)

    protected abstract fun getTaskName(): Text

    override fun toString(): String {
        return getTaskName().text!!
    }

    override fun tryUnfork(): Boolean {
        throw RuntimeException(OperationNotSupportedException())
    }

    override fun complete(value: Optional<T>?) {
        super.complete(value)
        stateFinish(fetchTaskState(), value?.orElse(null))
    }

    override fun completeExceptionally(ex: Throwable) {
        super.completeExceptionally(ex)
        stateExc(fetchTaskState(), ex)
    }

    fun submitTo(pool: ForkJoinPool): AbstractTask<T> {
        pool.execute(this)
        return this
    }

    companion object {
        @JvmStatic
        fun printTextData(taskState: TaskState<*>?, output: Consumer<String>?) {
            Optional.ofNullable(taskState)
                .map { it.message }
                .map { it?.text }
                .ifPresent(output!!)
        }

        @JvmStatic
        private val EVENT_LOGGER = LogManager.getLogger(
            AbstractTask::class.java
        )
    }
}