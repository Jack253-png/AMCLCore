package com.mcreater.amclcore

import com.mcreater.amclcore.account.AbstractAccount
import com.mcreater.amclcore.account.MicrosoftAccount
import com.mcreater.amclcore.account.auth.OAuth
import com.mcreater.amclcore.api.mihoyo.genshin.AbstractGenshinGachaApi
import com.mcreater.amclcore.concurrent.ConcurrentExecutors
import com.mcreater.amclcore.concurrent.task.AbstractTask
import com.mcreater.amclcore.game.GameInstance
import com.mcreater.amclcore.game.GameRepository
import com.mcreater.amclcore.java.JavaEnvironment
import com.mcreater.amclcore.java.MemorySize
import com.mcreater.amclcore.model.config.ConfigLaunchModel
import com.mcreater.amclcore.model.config.ConfigMainModel
import com.mcreater.amclcore.model.config.ConfigMemoryModel
import com.mcreater.amclcore.util.JsonUtil.GSON_PARSER
import com.mcreater.amclcore.util.PropertyUtil
import org.apache.logging.log4j.LogManager
import java.io.File
import java.util.*
import java.util.concurrent.ExecutionException
import java.util.function.Consumer

class Main {
    companion object {
        init {
            if (MetaData.isUseAnsiOutputOverride()) PropertyUtil.setProperty("log4j.skipJansi", false)
        }

        @JvmStatic
        private val logger = LogManager.getLogger(Main::class.java)

        @JvmStatic
        fun main(args: Array<String>) {
            GSON_PARSER.toJson(
                AbstractGenshinGachaApi.getCnInstance().genshinGachaFetchAsync()
                    .submitTo(ConcurrentExecutors.OAUTH_LOGIN_EXECUTOR).get().get(),
                System.out
            )
            println(loginTest()?.accountName)
            // launchTest(loginTest())
            /*GameRepository.of("D:\\basetest", "My minecraft repository")
                .ifPresent {
                    try {
                        VanillaInstallTask(
                            it,
                            "1.20.1",
                            "1.18.2-installer",
                            MinecraftMirroredResourceURL.MirrorServer.MCBBS
                        ).submitTo(ConcurrentExecutors.DOWNLOAD_QUEUE_EXECUTOR).get()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }*/

        }

        private fun launchTest(account: AbstractAccount?) {
            GameRepository.of("D:\\mods\\minecraft\\.minecraft", "My minecraft repository")
                .ifPresent { repository: GameRepository ->
                    try {
                        repository.updateAsync().submitTo(ConcurrentExecutors.EVENT_QUEUE_EXECUTOR).get()
                    } catch (e: InterruptedException) {
                        e.printStackTrace()
                    } catch (e: ExecutionException) {
                        e.printStackTrace()
                    }
                    repository.instances.forEach(Consumer { gameInstance: GameInstance ->
                        println(
                            gameInstance.instanceName
                        )
                    })
                    val gameInstance = repository.instances[5]
                    try {
                        gameInstance.launchAsync(
                            ConfigMainModel.builder()
                                .launchConfig(
                                    ConfigLaunchModel.builder()
                                        .environments(
                                            listOf(
                                                JavaEnvironment.create(File("C:\\Program Files\\Java\\jre1.8.0_351\\bin\\java.exe")),
                                                JavaEnvironment.create(File("C:\\Program Files\\Java\\jdk-17.0.1\\bin\\java.exe"))
                                            )
                                        )
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
                                .accounts(listOf(account))
                                .selectedAccountIndex(0)
                                .build()
                        )
                            .submitTo(ConcurrentExecutors.LAUNCH_EVENT_EXECUTOR)
                            .get()
                    } catch (e: java.lang.Exception) {
                        e.printStackTrace()
                    }
                }
        }

        @JvmStatic
        @Throws(java.lang.Exception::class)
        fun loginTest(): MicrosoftAccount? {
            // TODO test offline
            ConcurrentExecutors.OAUTH_LOGIN_EXECUTOR.wrappedListeners.add(Consumer { logger.info(it) })
            val account = OAuth.MICROSOFT.deviceCodeLoginAsync(OAuth.defaultDevHandler)
                .addStateConsumer {
                    AbstractTask.printTextData(it) { message: String? -> logger.info(message) }
                }
                .addBindConsumer {

                }
                .submitTo(ConcurrentExecutors.OAUTH_LOGIN_EXECUTOR)
                .get()

            return account.orElse(null)
        }
    }
}