package com.mcreater.amclcore.exceptions.installation;

import java.io.IOException;

import static com.mcreater.amclcore.i18n.I18NManager.translatable;

public class MinecraftVersionNotFountException extends IOException {
    public String toString() {
        return translatable("core.exception.version_not_found").getText();
    }
}
