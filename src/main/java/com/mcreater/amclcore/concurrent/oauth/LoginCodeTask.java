package com.mcreater.amclcore.concurrent.oauth;

import com.mcreater.amclcore.account.auth.OAuth;
import com.mcreater.amclcore.concurrent.AbstractTask;

public class LoginCodeTask extends AbstractTask<OAuth.DeviceCodeModel> {

    public OAuth.DeviceCodeModel call() throws Exception {
        return OAuth.MICROSOFT.createDeviceToken();
    }
}
