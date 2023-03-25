package com.mcreater.amclcore.concurrent;

import com.mcreater.amclcore.i18n.I18NManager;
import lombok.Builder;
import lombok.Data;

public class TaskStates {
    @Data
    @Builder
    public static class SimpleTaskState {
        private I18NManager.TranslatableText text;
    }

    @Data
    @Builder
    public static class SimpleTaskStateWithArg<T> {
        private I18NManager.TranslatableText text;
        private T arg;

        public static <T> SimpleTaskStateWithArg<T> create(I18NManager.TranslatableText text, T arg) {
            return SimpleTaskStateWithArg.<T>builder()
                    .arg(arg)
                    .text(text)
                    .build();
        }
    }
}
