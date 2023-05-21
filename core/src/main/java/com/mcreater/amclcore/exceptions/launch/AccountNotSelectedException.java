package com.mcreater.amclcore.exceptions.launch;

import java.io.IOException;

import static com.mcreater.amclcore.i18n.I18NManager.translatable;

public class AccountNotSelectedException extends IOException {
    public String toString() {
        return translatable("core.exceptions.launchAsync.account_not_sel").getText();
    }
}
