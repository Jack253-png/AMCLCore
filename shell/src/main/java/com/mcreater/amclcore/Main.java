package com.mcreater.amclcore;

import com.mcreater.amclcore.account.AbstractAccount;
import com.mcreater.amclcore.account.MicrosoftAccount;
import com.mcreater.amclcore.account.OfflineAccount;
import com.mcreater.amclcore.account.auth.OAuth;
import com.mcreater.amclcore.account.auth.YggdrasilAuthServer;
import com.mcreater.amclcore.concurrent.ConcurrentExecutors;
import com.mcreater.amclcore.concurrent.task.AbstractTask;
import com.mcreater.amclcore.game.GameInstance;
import com.mcreater.amclcore.game.GameRepository;
import com.mcreater.amclcore.java.JavaEnvironment;
import com.mcreater.amclcore.java.MemorySize;
import com.mcreater.amclcore.model.config.ConfigLaunchModel;
import com.mcreater.amclcore.model.config.ConfigMainModel;
import com.mcreater.amclcore.model.config.ConfigMemoryModel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.Optional;
import java.util.Vector;
import java.util.concurrent.ExecutionException;

import static com.mcreater.amclcore.MetaData.isUseAnsiOutputOverride;
import static com.mcreater.amclcore.util.PropertyUtil.setProperty;

public class Main {
    static {
        if (isUseAnsiOutputOverride()) setProperty("log4j.skipJansi", false);
    }

    private static final Logger logger = LogManager.getLogger(Main.class);

    public static void main(String[] args) throws Exception {
        loginTest();
//        launchTest();

        YggdrasilAuthServer server = new YggdrasilAuthServer(9801);
        server.getAccounts().add(
                OfflineAccount.create("test", OfflineAccount.STEVE)
        );
        server.start();
        while (true) {
        }
    }

    public static void launchTest() {
        GameRepository.of("D:\\mods\\minecraft\\.minecraft", "My minecraft repository").ifPresent(repository -> {
            try {
                repository.updateAsync().submitTo(ConcurrentExecutors.EVENT_QUEUE_EXECUTOR).get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }

            repository.getInstances().forEach(gameInstance -> System.out.println(gameInstance.getInstanceName()));

            GameInstance gameInstance = repository.getInstances().get(repository.getInstances().size() - 4);

            try {
                gameInstance.launchAsync(
                        ConfigMainModel.builder()
                                .launchConfig(
                                        ConfigLaunchModel.builder()
                                                .environments(new Vector<JavaEnvironment>() {{
                                                    add(JavaEnvironment.create(new File("C:\\Program Files\\Java\\jre1.8.0_351\\bin\\java.exe")));
                                                    add(JavaEnvironment.create(new File("C:\\Program Files\\Java\\jdk-17.0.1\\bin\\java.exe")));
                                                }})
                                                .selectedEnvironment(1)
                                                .useSelfGamePath(true)
                                                .memory(
                                                        ConfigMemoryModel.builder()
                                                                .maxMemory(MemorySize.createMegaBytes(8192))
                                                                .minMemory(MemorySize.createMegaBytes(128))
                                                                .build()
                                                )
                                                .build()
                                )
                                .accounts(new Vector<AbstractAccount>() {{
                                    add(OfflineAccount.create("test", OfflineAccount.STEVE));
                                }})
                                .selectedAccountIndex(0)
                                .build()
                        )
                        .submitTo(ConcurrentExecutors.LAUNCH_EVENT_EXECUTOR)
                        .get();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public static void loginTest() throws Exception {
        // TODO test offline
        ConcurrentExecutors.OAUTH_LOGIN_EXECUTOR.getWrappedListeners().add(logger::info);

        Optional<MicrosoftAccount> account = OAuth.MICROSOFT.deviceCodeLoginAsync(OAuth.defaultDevHandler)
                .addStateConsumer(state -> AbstractTask.printTextData(state, logger::info))
                .addBindConsumer(t -> t.addStateConsumer(state -> AbstractTask.printTextData(state, logger::info)))
                .submitTo(ConcurrentExecutors.OAUTH_LOGIN_EXECUTOR)
                .get();

        account.ifPresent(a -> {
            logger.info(a.getSkins());
            logger.info(a.getCapes());
            try {
                a.changeAccountNameAsync("StarcloudSea")
                        .submitTo(ConcurrentExecutors.OAUTH_EVENT_EXECUTOR)
                        .get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        });
    }
}
