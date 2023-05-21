package com.mcreater.amclcore.concurrent.task.model;

import com.mcreater.amclcore.concurrent.task.AbstractAction;
import com.mcreater.amclcore.i18n.Text;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;

import static com.mcreater.amclcore.i18n.I18NManager.fixed;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class RunnableAction extends AbstractAction {
    private final Runnable runnable;

    protected void execute() {
        runnable.run();
    }

    protected Text getTaskName() {
        return fixed("");
    }

    public static RunnableAction of(Runnable runnable) {
        return new RunnableAction(runnable);
    }
}
