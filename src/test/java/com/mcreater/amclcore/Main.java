package com.mcreater.amclcore;

import com.mcreater.amclcore.concurrent.AbstractTask;
import com.mcreater.amclcore.concurrent.ConcurrentExecutors;

public class Main {
    public static void main(String[] args) throws InterruptedException {
//        System.out.println(TypeToken.getParameterized(Class.class, String.class).getRawType());
//        OAuth.MICROSOFT.createDeviceToken();

        for (int i = 0; i < 65; i++) {
            ConcurrentExecutors.EVENT_QUEUE_EXECUTOR.execute(new AbstractTask<Object>() {
                public Object call() {
                    return null;
                }
            });
        }

        while (ConcurrentExecutors.EVENT_QUEUE_EXECUTOR.getActiveCount() > 1) {
            System.out.println(ConcurrentExecutors.EVENT_QUEUE_EXECUTOR.getActiveCount());
            System.out.println(ConcurrentExecutors.EVENT_QUEUE_EXECUTOR.getQueue());
            Thread.sleep(1000);
        }
    }
}