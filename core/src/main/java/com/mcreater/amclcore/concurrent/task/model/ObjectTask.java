package com.mcreater.amclcore.concurrent.task.model;

import com.mcreater.amclcore.concurrent.task.AbstractTask;
import com.mcreater.amclcore.i18n.Text;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;

import static com.mcreater.amclcore.i18n.I18NManager.fixed;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ObjectTask<T> extends AbstractTask<T> {
    private T result;

    protected T call() {
        return null;
    }

    protected Text getTaskName() {
        return fixed("");
    }

    public static <T> ObjectTask<T> of(T result) {
        return new ObjectTask<>(result);
    }
}
