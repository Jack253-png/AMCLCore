package com.mcreater.amclcore.concurrent

import com.mcreater.amclcore.i18n.Text
import java.util.function.Consumer

data class TaskState<R>(
    var taskType: Type = Type.EXECUTING,
    var throwable: Throwable? = null,
    var result: R? = null,
    var totalStage: Int = 1,
    var currentStage: Int = 1,
    var message: Text? = null
) {
    enum class Type {
        ERROR,
        EXECUTING,
        FINISHED
    }

    fun getStageDouble(): Double {
        return if (totalStage != 0) currentStage.toDouble() / totalStage else 0.0
    }

    fun getStateInt(): Int {
        return (getStageDouble() * 100).toInt()
    }

    fun isExc(): Boolean {
        return taskType == Type.ERROR
    }

    fun executeIfExc(c: Consumer<Throwable?>) {
        if (isExc() && throwable != null) c.accept(throwable)
    }

    fun isFinish(): Boolean {
        return taskType == Type.FINISHED
    }

    fun executeIfFinish(c: Consumer<R>) {
        if (isFinish() && result != null) c.accept(result!!)
    }
}
