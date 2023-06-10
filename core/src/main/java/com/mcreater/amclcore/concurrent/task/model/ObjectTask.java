package com.mcreater.amclcore.concurrent.task.model;

import com.mcreater.amclcore.concurrent.task.AbstractTask;
import com.mcreater.amclcore.i18n.Text;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;

import static com.mcreater.amclcore.i18n.I18NManager.translatable;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ObjectTask<T> extends AbstractTask<T> {
    private T result;
    private Text text;

    protected T call() {
        return null;
    }

    protected Text getTaskName() {
        return text;
    }

    public static <T> ObjectTask<T> of(T result) {
        return new ObjectTask<>(result, translatable("core.concurrent.task.model.empty"));
    }

    public static <T> ObjectTask<T> of(T result, Text text) {
        return new ObjectTask<>(result, text);
    }
}
