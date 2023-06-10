package com.mcreater.amclcore.concurrent.task.model;

import com.mcreater.amclcore.concurrent.task.AbstractAction;
import com.mcreater.amclcore.i18n.Text;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;

import static com.mcreater.amclcore.i18n.I18NManager.translatable;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class RunnableAction extends AbstractAction {
    private final Runnable runnable;
    private Text text;

    protected void execute() {
        runnable.run();
    }

    protected Text getTaskName() {
        return text;
    }

    public static RunnableAction of(Runnable runnable) {
        return new RunnableAction(runnable, translatable("core.concurrent.task.model.empty"));
    }

    public static RunnableAction of(Runnable runnable, Text text) {
        return new RunnableAction(runnable, text);
    }
}
