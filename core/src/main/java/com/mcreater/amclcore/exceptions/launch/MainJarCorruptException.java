package com.mcreater.amclcore.exceptions.launch;

import java.io.IOException;

import static com.mcreater.amclcore.i18n.I18NManager.translatable;

public class MainJarCorruptException extends IOException {
    public String toString() {
        return translatable("core.exceptions.launchAsync.mainjar_corrupt").getText();
    }
}
