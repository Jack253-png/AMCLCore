package com.mcreater.amclcore;

import com.mcreater.amclcore.account.AbstractAccount;
import com.mcreater.amclcore.account.MicrosoftAccount;
import com.mcreater.amclcore.account.OfflineAccount;
import com.mcreater.amclcore.account.auth.OAuth;
import com.mcreater.amclcore.concurrent.ConcurrentExecutors;
import com.mcreater.amclcore.concurrent.task.AbstractTask;
import com.mcreater.amclcore.game.GameInstance;
import com.mcreater.amclcore.game.GameRepository;
import com.mcreater.amclcore.java.JavaEnvironment;
import com.mcreater.amclcore.java.MemorySize;
import com.mcreater.amclcore.model.config.ConfigLaunchModel;
import com.mcreater.amclcore.model.config.ConfigMainModel;
import com.mcreater.amclcore.model.config.ConfigMemoryModel;
import com.mcreater.amclcore.nbtlib.common.tags.NamedTag;
import com.mcreater.amclcore.nbtlib.nbt.io.NBTInput;
import com.mcreater.amclcore.nbtlib.nbt.io.NBTOutput;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.nio.file.Files;
import java.util.Collections;
import java.util.Optional;
import java.util.Vector;
import java.util.concurrent.ExecutionException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import static com.mcreater.amclcore.MetaData.isUseAnsiOutputOverride;
import static com.mcreater.amclcore.nbtlib.nbt.NBTIOUtil.createInput;
import static com.mcreater.amclcore.nbtlib.nbt.NBTIOUtil.createOutput;
import static com.mcreater.amclcore.util.PropertyUtil.setProperty;

public class Main {
    static {
        if (isUseAnsiOutputOverride()) setProperty("log4j.skipJansi", false);
    }

    private static final Logger logger = LogManager.getLogger(Main.class);

    public static void main(String[] args) throws Exception {
//        loginTest();
//        launchTest();
        NBTInput stream = createInput(
                new GZIPInputStream(
                        Files.newInputStream(
                                new File("D:\\mods\\minecraft\\.minecraft\\versions\\MTR Client\\saves\\world\\level.dat").toPath()
                        )
                )
        );

        NamedTag tg = stream.readTag(Integer.MAX_VALUE);
        stream.close();

        System.out.println("\n\n\n\n\n\n\n\n\n\n\n\n");

        Files.delete(new File("D:\\test.dat").toPath());
        NBTOutput stream1 = createOutput(
                new GZIPOutputStream(
                        Files.newOutputStream(
                                new File("D:\\test.dat").toPath()
                        )
                )
        );
        stream1.writeTag(tg, Integer.MAX_VALUE);
        stream1.close();

        NBTInput stream2 = createInput(
                new GZIPInputStream(
                        Files.newInputStream(
                                new File("D:\\test.dat").toPath()
                        )
                )
        );
        System.out.println(stream2.readTag(Integer.MAX_VALUE));
        stream2.close();
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
                                                .environments(Collections.singletonList(JavaEnvironment.create(new File("C:\\Program Files\\Java\\jre1.8.0_351\\bin\\java.exe"))))
                                                .selectedEnvironment(0)
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
                );
//                        .submitTo(ConcurrentExecutors.LAUNCH_EVENT_EXECUTOR)
//                        .get();
            } catch (Exception e) {
                e.printStackTrace();
            }

            /*repository.getInstances().forEach(a -> {
                try {
                    System.out.println(a.getInstanceName());
                    System.out.println(a.fetchAddonsAsync().submitTo(ConcurrentExecutors.EVENT_QUEUE_EXECUTOR).get());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });*/

            /*LuaBaseApiLoader.init();
            LuaBaseApiLoader.load();

            System.out.println(I18NManager.translatable("aee").getText());*/
        });
    }

    public static void loginTest() throws Exception {
        // TODO test offline
//        HttpClientWrapper.setProxy(new HttpHost(InetAddress.getLocalHost()));
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
