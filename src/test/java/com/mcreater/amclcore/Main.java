package com.mcreater.amclcore;

import com.mcreater.amclcore.account.auth.OAuth;
import com.mcreater.amclcore.concurrent.ConcurrentExecutors;
import com.mcreater.amclcore.util.SwingUtils;

import java.util.concurrent.CompletableFuture;

import static com.mcreater.amclcore.util.JsonUtil.GSON_PARSER;

public class Main {
    public static void main(String[] args) throws Exception {
        CompletableFuture.runAsync(
                OAuth.MICROSOFT.fetchDeviceTokenAsync(model -> {
                    ConcurrentExecutors.runAllTask(
                        ConcurrentExecutors.AWT_EVENT_EXECUTOR,
                        SwingUtils.copyContentAsync(model.getUserCode()),
                        SwingUtils.openBrowserAsync(model.getVerificationUri())
                    );

                    while (true) {
                        try {
                            System.out.println(GSON_PARSER.toJson(OAuth.MICROSOFT.checkToken(model.getDeviceCode())));
                            Thread.sleep(1000);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }),
                ConcurrentExecutors.EVENT_QUEUE_EXECUTOR
        ).get();
    }
}