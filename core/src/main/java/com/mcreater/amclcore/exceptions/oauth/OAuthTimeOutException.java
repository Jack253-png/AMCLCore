package com.mcreater.amclcore.exceptions.oauth;

import java.io.IOException;

import static com.mcreater.amclcore.i18n.I18NManager.translatable;

/**
 * throw this exception when oauth code time out
 */
public class OAuthTimeOutException extends IOException {
    public String toString() {
        return translatable("core.exceptions.oauth.timeout").getText();
    }
}
