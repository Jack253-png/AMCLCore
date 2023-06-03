package com.mcreater.amclcore.exceptions.oauth;

import lombok.AllArgsConstructor;

import java.io.IOException;

import static com.mcreater.amclcore.i18n.I18NManager.translatable;

@AllArgsConstructor
public class OAuthMinecraftNameChangeNotAllowedException extends IOException {
    private final String lastChange;
    private final String nextChange;
    private final long deltaTime;

    public String toString() {
        return translatable("core.exceptions.oauth.name_change_not_allowed", lastChange, nextChange, deltaTime).getText();
    }
}
