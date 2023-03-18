package com.mcreater.amclcore;

import com.mcreater.amclcore.account.auth.OAuth;
import com.mcreater.amclcore.concurrent.ConcurrentExecutors;
import com.mcreater.amclcore.i18n.I18NManager;
import com.mcreater.amclcore.model.oauth.XBLUserModel;

public class Main {
    public static void main(String[] args) throws Exception {
        I18NManager.test();
        System.setProperty("log4j.skipJansi", "false");
    }

    public static void loginTest() throws Exception {
        XBLUserModel model = ConcurrentExecutors.submit(
                ConcurrentExecutors.EVENT_QUEUE_EXECUTOR,
                OAuth.MICROSOFT.fetchDeviceTokenAsync(OAuth.getDefaultDevHandler())
        ).get();
        System.out.println(model.getHash());
    }
}