package com.mcreater.amclcore.concurrent.task.model;

import com.mcreater.amclcore.concurrent.task.AbstractTask;
import com.mcreater.amclcore.i18n.Text;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;

import static com.mcreater.amclcore.i18n.I18NManager.fixed;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class BooleanTask extends AbstractTask<Boolean> {
    private boolean result;

    protected Boolean call() {
        return result;
    }

    protected Text getTaskName() {
        return fixed("");
    }

    public static BooleanTask of(boolean b) {
        return new BooleanTask(b);
    }
}
