package com.mcreater.amclcore.concurrent.task.model;

import com.mcreater.amclcore.concurrent.task.AbstractTask;
import com.mcreater.amclcore.i18n.Text;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;

import static com.mcreater.amclcore.i18n.I18NManager.translatable;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class BooleanTask extends AbstractTask<Boolean> {
    private boolean result;
    private Text text;

    protected Boolean call() {
        return result;
    }

    protected Text getTaskName() {
        return text;
    }

    public static BooleanTask of(boolean b) {
        return new BooleanTask(b, translatable("core.concurrent.task.model.empty"));
    }

    public static BooleanTask of(boolean b, Text text) {
        return new BooleanTask(b, text);
    }
}
