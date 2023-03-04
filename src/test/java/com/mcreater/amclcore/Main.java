package com.mcreater.amclcore;

import com.mcreater.amclcore.account.auth.OAuth;
import com.mcreater.amclcore.concurrent.ConcurrentExecutors;
import com.mcreater.amclcore.model.oauth.DeviceCodeConverterModel;
import com.mcreater.amclcore.util.SwingUtil;

public class Main {
    public static void main(String[] args) throws Exception {
        DeviceCodeConverterModel model = ConcurrentExecutors.EVENT_QUEUE_EXECUTOR.submit(
                ConcurrentExecutors.fromTask(OAuth.MICROSOFT.fetchDeviceTokenAsync(model2 -> ConcurrentExecutors.runAllTask(
                        ConcurrentExecutors.AWT_EVENT_EXECUTOR,
                        SwingUtil.copyContentAsync(model2.getUserCode()),
                        SwingUtil.openBrowserAsync(model2.getVerificationUri())
                )))
        ).get();

        System.out.println(model.getModel().getAccessToken());
    }
}