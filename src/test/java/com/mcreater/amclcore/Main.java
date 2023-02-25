package com.mcreater.amclcore;

import com.mcreater.amclcore.account.auth.OAuth;
import com.mcreater.amclcore.concurrent.ConcurrentExecutors;

import java.util.concurrent.CompletableFuture;

public class Main {
    public static void main(String[] args) throws Exception {
        CompletableFuture.runAsync(OAuth.MICROSOFT.createDeviceTokenAsync(), ConcurrentExecutors.EVENT_QUEUE_EXECUTOR).get();
    }
}