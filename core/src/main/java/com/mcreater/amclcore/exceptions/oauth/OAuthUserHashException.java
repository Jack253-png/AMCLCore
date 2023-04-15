package com.mcreater.amclcore.exceptions.oauth;

import static com.mcreater.amclcore.i18n.I18NManager.translatable;

/**
 * throw this exception when user hash didn't equal
 */
public class OAuthUserHashException extends IllegalStateException {
    public String toString() {
        return translatable("core.exceptions.oauth.user_hash").getText();
    }
}
