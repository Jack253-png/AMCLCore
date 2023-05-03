package com.mcreater.amclcore.exceptions.launch;

import java.io.IOException;

import static com.mcreater.amclcore.i18n.I18NManager.translatable;

public class ManifestJsonCorruptException extends IOException {
    public String toString() {
        return translatable("core.exceptions.launch.manifest_corrupt").getText();
    }
}
