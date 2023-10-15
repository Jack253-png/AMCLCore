package com.mcreater.amclcore.concurrent.task.model

import com.mcreater.amclcore.concurrent.task.AbstractTask
import com.mcreater.amclcore.i18n.I18NManager.Companion.translatable
import com.mcreater.amclcore.i18n.Text


data class BooleanTask(
    val result: Boolean = false,
    val text: Text? = null
) : AbstractTask<Boolean?>() {

    override fun call(): Boolean {
        return result
    }

    override fun getTaskName(): Text {
        return text!!
    }

    companion object {
        @JvmStatic
        fun of(b: Boolean): BooleanTask {
            return BooleanTask(b, translatable("core.concurrent.task.model.empty"))
        }

        @JvmStatic
        fun of(b: Boolean, text: Text?): BooleanTask {
            return BooleanTask(b, text)
        }
    }
}

