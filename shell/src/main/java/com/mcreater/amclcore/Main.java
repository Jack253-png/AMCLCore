package com.mcreater.amclcore;

import com.mcreater.amclcore.account.auth.OAuth;
import com.mcreater.amclcore.concurrent.ConcurrentExecutors;
import com.mcreater.amclcore.concurrent.TaskState;
import com.mcreater.amclcore.concurrent.TaskStates;
import com.mcreater.amclcore.i18n.I18NManager;
import com.mcreater.amclcore.model.oauth.MinecraftRequestModel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Optional;

public class Main {
    static {
        System.setProperty("log4j.skipJansi", "false");
    }
    private static final Logger logger = LogManager.getLogger(Main.class);

    public static void main(String[] args) throws Exception {
        loginTest();
    }

    public static void loginTest() throws Exception {
        Optional<MinecraftRequestModel> model = ConcurrentExecutors.OAUTH_LOGIN_EXECUTOR.submit(
                OAuth.MICROSOFT.fetchDeviceTokenAsync(OAuth.getDefaultDevHandler())
                        .addStateConsumer(c -> Optional.ofNullable(c)
                                .map(TaskState::getData)
                                .map(TaskStates.SimpleTaskStateWithArg::getText)
                                .map(I18NManager.TranslatableText::getText)
                                .ifPresent(logger::info)
                        )
        ).get();

        System.out.println(model.orElse(null));
    }
}
