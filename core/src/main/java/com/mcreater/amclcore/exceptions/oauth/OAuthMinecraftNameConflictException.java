package com.mcreater.amclcore.exceptions.oauth;

import java.io.IOException;

import static com.mcreater.amclcore.i18n.I18NManager.translatable;

public class OAuthMinecraftNameConflictException extends IOException {
    public String toString() {
        return translatable("core.exceptions.oauth.name_conflict").getText();
    }
}
