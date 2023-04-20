package com.mcreater.amclcore.exceptions.oauth;

import java.io.IOException;

import static com.mcreater.amclcore.i18n.I18NManager.translatable;

public class OAuthMinecraftStoreCheckException extends IOException {
    public String toString() {
        return translatable("core.exceptions.oauth.store_check").getText();
    }
}
