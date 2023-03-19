package com.mcreater.amclcore;

import com.mcreater.amclcore.account.auth.OAuth;
import com.mcreater.amclcore.concurrent.ConcurrentExecutors;
import com.mcreater.amclcore.model.oauth.XBLUserModel;

import java.util.Optional;

public class Main {
    public static void main(String[] args) throws Exception {
        System.setProperty("log4j.skipJansi", "false");
        loginTest();
    }

    public static void loginTest() throws Exception {
        Optional<XBLUserModel> model = ConcurrentExecutors.submit(
                ConcurrentExecutors.EVENT_QUEUE_EXECUTOR,
                OAuth.MICROSOFT.fetchDeviceTokenAsync(OAuth.getDefaultDevHandler())
        ).get();
        System.out.println(model.orElse(null));
    }
}