package com.mcreater.amclcore.concurrent

import com.mcreater.amclcore.concurrent.task.AbstractTask
import java.util.*
import java.util.concurrent.Callable
import java.util.concurrent.ForkJoinPool
import java.util.concurrent.ForkJoinTask
import java.util.function.Consumer


class AdvancedForkJoinPool(
    parallelism: Int,
    factory: ForkJoinWorkerThreadFactory?,
    handler: Thread.UncaughtExceptionHandler?,
    asyncMode: Boolean
) : ForkJoinPool(parallelism, factory, handler, asyncMode) {
    private val tasks: MutableList<AbstractTask<*>> = Vector()
    private val allTasks: MutableList<ForkJoinTask<*>> = Vector()

    val baseListeners: MutableList<Consumer<ForkJoinTask<*>>> = Vector()
    val wrappedListeners: MutableList<Consumer<AbstractTask<*>>> = Vector()
    private val eventPool = ConcurrentExecutors.createInterfaceEventExecutor()
    fun getTasks(): List<AbstractTask<*>> {
        return Collections.unmodifiableList(tasks)
    }

    fun getAllTasks(): List<ForkJoinTask<*>> {
        return Collections.unmodifiableList(allTasks)
    }

    private fun <T> processTask(task: ForkJoinTask<T>): ForkJoinTask<T> {
        allTasks.add(task)
        if (task is AbstractTask<*>) tasks.add(task as AbstractTask<*>)
        eventPool.execute {
            baseListeners.forEach(Consumer { t: Consumer<ForkJoinTask<*>> ->
                t.accept(
                    task
                )
            })
            if (task is AbstractTask<*>) wrappedListeners.forEach(Consumer { t: Consumer<AbstractTask<*>> ->
                t.accept(
                    task as AbstractTask<*>
                )
            })
        }
        return task
    }

    override fun submit(task: Runnable): ForkJoinTask<*> {
        return Optional.of(super.submit(task))
            .map { processTask(it) }.get()
    }

    override fun <T> submit(task: Callable<T>): ForkJoinTask<T> {
        return Optional.of(super.submit(task)).map { processTask(it) }.get()
    }

    override fun <T> submit(task: ForkJoinTask<T>): ForkJoinTask<T> {
        return Optional.of(super.submit(task)).map { processTask(it) }.get()
    }

    override fun <T> submit(task: Runnable, result: T): ForkJoinTask<T> {
        return Optional.of(super.submit(task, result)).map { processTask(it) }.get()
    }

    override fun execute(task: Runnable) {
        processTask(super.submit(task))
    }

    override fun execute(task: ForkJoinTask<*>?) {
        processTask(super.submit(task))
    }
}
