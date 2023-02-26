package com.mcreater.amclcore;

import com.mcreater.amclcore.account.auth.OAuth;
import com.mcreater.amclcore.concurrent.ConcurrentExecutors;
import com.mcreater.amclcore.util.SwingUtils;

import java.util.concurrent.CompletableFuture;

public class Main {
    public static void main(String[] args) throws Exception {
        CompletableFuture.runAsync(
                OAuth.MICROSOFT.fetchDeviceTokenAsync(model -> ConcurrentExecutors.runAllTask(
                        ConcurrentExecutors.AWT_EVENT_EXECUTOR,
                        SwingUtils.copyContentAsync(model.getUserCode()),
                        SwingUtils.openBrowserAsync(model.getVerificationUri())
                )),
                ConcurrentExecutors.EVENT_QUEUE_EXECUTOR
        ).get();
    }
}