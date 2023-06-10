package com.mcreater.amclcore.concurrent.task.model;

import com.mcreater.amclcore.concurrent.task.AbstractAction;
import com.mcreater.amclcore.i18n.Text;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;

import static com.mcreater.amclcore.i18n.I18NManager.translatable;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class EmptyAction extends AbstractAction {
    protected void execute() {
    }

    protected Text getTaskName() {
        return translatable("core.concurrent.task.model.empty");
    }

    public static EmptyAction of() {
        return new EmptyAction();
    }
}
