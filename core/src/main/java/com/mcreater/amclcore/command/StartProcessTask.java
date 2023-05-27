package com.mcreater.amclcore.command;

import com.mcreater.amclcore.concurrent.ConcurrentExecutors;
import com.mcreater.amclcore.concurrent.ExtendForkJoinPool;
import com.mcreater.amclcore.concurrent.task.AbstractTask;
import com.mcreater.amclcore.concurrent.task.model.RunnableAction;
import com.mcreater.amclcore.i18n.Text;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import static com.mcreater.amclcore.concurrent.ConcurrentExecutors.excHandler;
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
            OutputParser.OutputLine.create(s, ps).printAnsi();
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

        return process.exitValue();
    }

    protected Text getTaskName() {
        return translatable("core.task.process.name");
    }
}
