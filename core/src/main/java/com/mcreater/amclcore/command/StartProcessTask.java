package com.mcreater.amclcore.command;

import com.mcreater.amclcore.concurrent.ConcurrentExecutors;
import com.mcreater.amclcore.concurrent.ExtendForkJoinPool;
import com.mcreater.amclcore.concurrent.TaskState;
import com.mcreater.amclcore.concurrent.task.AbstractTask;
import com.mcreater.amclcore.concurrent.task.model.RunnableAction;
import com.mcreater.amclcore.i18n.Text;
import lombok.Getter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.Vector;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import static com.mcreater.amclcore.concurrent.ConcurrentExecutors.excHandler;
import static com.mcreater.amclcore.i18n.I18NManager.fixed;
import static com.mcreater.amclcore.i18n.I18NManager.translatable;

public class StartProcessTask extends AbstractTask<Integer> {
    public static StartProcessTask create(List<CommandArg> args) {
        return new StartProcessTask(args);
    }

    private StartProcessTask(List<CommandArg> args) {
        this.args = args;
    }

    private final List<CommandArg> args;
    private Path startPath;
    private Process process;
    @Getter
    private final List<OutputParser.OutputLine> logLines = new Vector<>();
    private final ExtendForkJoinPool pool = new ExtendForkJoinPool(
            2,
            ConcurrentExecutors.ForkJoinWorkerThreadFactoryImpl.INSTANCE,
            excHandler,
            true
    );

    public StartProcessTask setStartPath(Path path) {
        this.startPath = path;
        return this;
    }

    protected Integer call() throws Exception {
        setState(
                TaskState.<Integer>builder()
                        .totalStage(2)
                        .currentStage(0)
                        .build()
        );
        process = new ProcessBuilder().command(
                        args.stream()
                                .map(CommandArg::toString)
                                .collect(Collectors.toList())
                )
                .directory(startPath.toFile())
                .start();
        Runtime.getRuntime().addShutdownHook(new Thread(this.process::destroy));

        BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8));
        BufferedReader err = new BufferedReader(new InputStreamReader(process.getErrorStream(), StandardCharsets.UTF_8));

        BiConsumer<String, PrintStream> consumer = (s, ps) -> {
            Optional.of(OutputParser.OutputLine.create(s, ps)).map(a -> {
                logLines.add(a);
                return a;
            }).get().printAnsi();
            setState(
                    TaskState.<Integer>builder()
                            .totalStage(2)
                            .currentStage(1)
                            .message(fixed(s))
                            .build()
            );
        };

        RunnableAction.of(() -> {
            do {
                try {
                    Optional.ofNullable(in.readLine()).ifPresent(a -> {
                        consumer.accept(a, System.out);
                    });
                } catch (IOException ignored) {
                }
            }
            while (process.isAlive());
        }).submitTo(pool);

        RunnableAction.of(() -> {
            do {
                try {
                    Optional.ofNullable(err.readLine()).ifPresent(a -> {
                        consumer.accept(a, System.err);
                    });
                } catch (IOException ignored) {
                }
            }
            while (process.isAlive());
        }).submitTo(pool);

        do {
        }
        while (process.isAlive());

        setState(
                TaskState.<Integer>builder()
                        .totalStage(2)
                        .currentStage(2)
                        .message(translatable("core.process.exit", process.exitValue()))
                        .build()
        );

        return process.exitValue();
    }

    protected Text getTaskName() {
        return translatable("core.task.process.name");
    }
}
