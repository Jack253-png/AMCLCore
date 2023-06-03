package com.mcreater.amclcore.i18n;

import java.util.Locale;

public interface Text {
    String getText(Locale locale);

    default String getText() {
        return getText(Locale.getDefault());
    }
}