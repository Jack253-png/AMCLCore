package com.mcreater.amclcore;

import com.mcreater.amclcore.account.auth.OAuth;
import com.mcreater.amclcore.concurrent.ConcurrentExecutors;
import com.mcreater.amclcore.model.oauth.DeviceCodeConverterModel;

public class Main {
    public static void main(String[] args) throws Exception {
        DeviceCodeConverterModel model = ConcurrentExecutors.EVENT_QUEUE_EXECUTOR.submit(
                ConcurrentExecutors.fromTask(
                        OAuth.MICROSOFT.fetchDeviceTokenAsync(OAuth.getDefaultDevHandler())
                )
        ).get();

        System.out.println(model.getModel().getAccessToken());
    }
}