package com.mcreater.amclcore;

import com.mcreater.amclcore.account.MicrosoftAccount;
import com.mcreater.amclcore.account.auth.OAuth;
import com.mcreater.amclcore.concurrent.ConcurrentExecutors;
import com.mcreater.amclcore.concurrent.task.AbstractTask;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Optional;

import static com.mcreater.amclcore.util.PropertyUtil.setProperty;

public class Main {
    static {
        setProperty("log4j.skipJansi", false);
    }
    private static final Logger logger = LogManager.getLogger(Main.class);
    public static void main(String[] args) throws Exception {
        loginTest();
    }

    public static void loginTest() throws Exception {
        // TODO test offline
//        HttpClientWrapper.setProxy(new HttpHost(InetAddress.getLocalHost()));
        ConcurrentExecutors.OAUTH_LOGIN_EXECUTOR.getWrappedListeners().add(logger::info);
        Optional<MicrosoftAccount> account = ConcurrentExecutors.OAUTH_LOGIN_EXECUTOR.submit(
                OAuth.MICROSOFT.deviceCodeLoginAsync(OAuth.defaultDevHandler)
                        .addStateConsumer(state -> AbstractTask.printTextData(state, logger::info))
                        .addBindConsumer(t -> t.addStateConsumer(state -> AbstractTask.printTextData(state, logger::info)))
        ).get();

        logger.info(account.orElse(null));
        logger.info(account.orElse(null).getSkins());
        logger.info(account.orElse(null).getCapes());
    }
}
