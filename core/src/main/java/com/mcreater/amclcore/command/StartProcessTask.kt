package com.mcreater.amclcore.command

import com.mcreater.amclcore.command.OutputParser.OutputLine
import com.mcreater.amclcore.command.OutputParser.OutputLine.Companion.create
import com.mcreater.amclcore.concurrent.ConcurrentExecutors
import com.mcreater.amclcore.concurrent.ExtendForkJoinPool
import com.mcreater.amclcore.concurrent.TaskState
import com.mcreater.amclcore.concurrent.task.AbstractTask
import com.mcreater.amclcore.concurrent.task.model.RunnableAction
import com.mcreater.amclcore.i18n.I18NManager.Companion.fixed
import com.mcreater.amclcore.i18n.I18NManager.Companion.translatable
import com.mcreater.amclcore.i18n.Text
import lombok.Getter
import java.io.*
import java.nio.charset.StandardCharsets
import java.nio.file.Path
import java.util.*
import java.util.function.BiConsumer
import java.util.stream.Collectors


class StartProcessTask private constructor(private val args: List<CommandArg>) :
    AbstractTask<Int?>() {
    private var startPath: Path? = null
    private var process: Process? = null

    @Getter
    private val logLines: MutableList<OutputLine> = Vector()
    private val pool = ExtendForkJoinPool(
        2,
        ConcurrentExecutors.ForkJoinWorkerThreadFactoryImpl.INSTANCE,
        ConcurrentExecutors.excHandler,
        true
    )

    fun setStartPath(path: Path?): StartProcessTask {
        startPath = path
        return this
    }

    @Throws(Exception::class)
    override fun call(): Int {
        setState(
            TaskState(
                totalStage = 2,
                currentStage = 0
            )
        )
        process = ProcessBuilder().command(
            args.stream()
                .map { obj: CommandArg -> obj.toString() }
                .collect(Collectors.toList())
        )
            .directory(startPath!!.toFile())
            .start()
        Runtime.getRuntime().addShutdownHook(Thread { process?.destroy() })
        val input = BufferedReader(
            InputStreamReader(
                process?.inputStream ?: ByteArrayInputStream(ByteArray(1)),
                StandardCharsets.UTF_8
            )
        )
        val err = BufferedReader(
            InputStreamReader(
                process?.errorStream ?: ByteArrayInputStream(ByteArray(1)),
                StandardCharsets.UTF_8
            )
        )
        val consumer =
            BiConsumer { s: String?, ps: PrintStream? ->
                Optional.of(
                    create(
                        s,
                        ps!!
                    )
                ).map { a: OutputLine ->
                    logLines.add(a)
                    a
                }.get().printAnsi()
                setState(
                    TaskState(
                        totalStage = 2,
                        currentStage = 1,
                        message = fixed(s)
                    )
                )
            }
        RunnableAction.of {
            do {
                try {
                    Optional.ofNullable(input.readLine())
                        .ifPresent { a: String ->
                            consumer.accept(
                                a,
                                System.out
                            )
                        }
                } catch (ignored: IOException) {
                }
            } while (process?.isAlive == true)
        }.submitTo(pool)
        RunnableAction.of {
            do {
                try {
                    Optional.ofNullable(err.readLine())
                        .ifPresent { a: String ->
                            consumer.accept(
                                a,
                                System.err
                            )
                        }
                } catch (ignored: IOException) {
                }
            } while (process?.isAlive == true)
        }.submitTo(pool)
        do {
        } while (process?.isAlive == true)
        setState(
            TaskState(
                totalStage = 2,
                currentStage = 2,
                message = translatable("core.process.exit", process?.exitValue() ?: -1)
            )
        )
        return process?.exitValue() ?: -1
    }

    override fun getTaskName(): Text {
        return translatable("core.task.process.name")
    }

    companion object {
        @JvmStatic
        fun create(args: List<CommandArg>): StartProcessTask {
            return StartProcessTask(args)
        }
    }
}
