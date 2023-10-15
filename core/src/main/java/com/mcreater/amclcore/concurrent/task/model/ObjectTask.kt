package com.mcreater.amclcore.concurrent.task.model

import com.mcreater.amclcore.concurrent.task.AbstractTask
import com.mcreater.amclcore.i18n.I18NManager.Companion.translatable
import com.mcreater.amclcore.i18n.Text


data class ObjectTask<T>(
    private val result: T,
    private val text: Text? = null
) : AbstractTask<T>() {
    override fun call(): T {
        return result
    }

    override fun getTaskName(): Text {
        return text!!
    }

    companion object {
        @JvmStatic
        fun <T : Any?> of(result: T): ObjectTask<T> {
            return ObjectTask(result, translatable("core.concurrent.task.model.empty"))
        }

        @JvmStatic
        fun <T : Any?> of(result: T, text: Text?): ObjectTask<T> {
            return ObjectTask(result, text)
        }
    }
}

