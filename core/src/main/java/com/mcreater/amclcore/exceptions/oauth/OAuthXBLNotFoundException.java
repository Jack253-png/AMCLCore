package com.mcreater.amclcore.exceptions.oauth;

import static com.mcreater.amclcore.i18n.I18NManager.translatable;

/**
 * throw this exception when XBox Live login failed
 */
public class OAuthXBLNotFoundException extends IllegalStateException {
    public String toString() {
        return translatable("core.exceptions.oauth.fail").getText();
    }
}
