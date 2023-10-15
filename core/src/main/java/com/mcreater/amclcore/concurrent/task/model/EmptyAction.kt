package com.mcreater.amclcore.concurrent.task.model

import com.mcreater.amclcore.concurrent.task.AbstractAction
import com.mcreater.amclcore.i18n.I18NManager.Companion.translatable
import com.mcreater.amclcore.i18n.Text


class EmptyAction : AbstractAction() {
    override fun execute() {}
    override fun getTaskName(): Text {
        return translatable("core.concurrent.task.model.empty")
    }

    companion object {
        @JvmStatic
        fun of(): EmptyAction {
            return EmptyAction()
        }
    }
}

