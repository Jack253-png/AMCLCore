package com.mcreater.amclcore.concurrent.task.model

import com.mcreater.amclcore.concurrent.task.AbstractAction
import com.mcreater.amclcore.i18n.I18NManager.Companion.translatable
import com.mcreater.amclcore.i18n.Text


data class RunnableAction(
    private val runnable: Runnable? = null,
    private val text: Text? = null
) : AbstractAction() {
    override fun execute() {
        runnable!!.run()
    }

    override fun getTaskName(): Text {
        return text!!
    }

    companion object {
        fun of(runnable: Runnable?): RunnableAction {
            return RunnableAction(runnable, translatable("core.concurrent.task.model.empty"))
        }

        fun of(runnable: Runnable?, text: Text?): RunnableAction {
            return RunnableAction(runnable, text)
        }
    }
}

