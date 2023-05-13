package com.mcreater.amclcore.game;

import com.mcreater.amclcore.command.CommandArg;
import com.mcreater.amclcore.concurrent.task.AbstractAction;
import com.mcreater.amclcore.exceptions.launch.ConfigCorruptException;
import com.mcreater.amclcore.exceptions.launch.MainJarCorruptException;
import com.mcreater.amclcore.exceptions.launch.ManifestJsonCorruptException;
import com.mcreater.amclcore.exceptions.report.ExceptionReporter;
import com.mcreater.amclcore.i18n.Text;
import com.mcreater.amclcore.java.JVMArgument;
import com.mcreater.amclcore.java.JavaEnvironment;
import com.mcreater.amclcore.java.MemorySize;
import com.mcreater.amclcore.model.config.ConfigMainModel;
import com.mcreater.amclcore.model.game.GameManifestJsonModel;
import com.mcreater.amclcore.model.game.assets.GameAssetsIndexFileModel;
import com.mcreater.amclcore.util.JsonUtil;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.stream.Collectors;

import static com.mcreater.amclcore.i18n.I18NManager.translatable;
import static com.mcreater.amclcore.util.JsonUtil.GSON_PARSER;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class GameInstance {
    private final GameRepository repository;
    private Path instancePath;
    private String instanceName;
    private GameManifestJson manifestJson;

    public static GameInstance createInstance(GameManifestJson json) {
        return new GameInstance(json.getRepository(), json.getVersionPath(), json.getName(), json);
    }

    public boolean checkIsValid() {
        try {
            manifestJson.readManifest();
            return true;
        } catch (Exception e) {
            ExceptionReporter.report(e, ExceptionReporter.ExceptionType.IO);
            return false;
        }
    }

    public AbstractAction fetchLaunchArgsAsync(ConfigMainModel config) {
        return new FetchLaunchArgsTask(config);
    }

    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public class FetchLaunchArgsTask extends AbstractAction {
        private ConfigMainModel config;

        protected void execute() throws Exception {
            List<CommandArg> args = new Vector<>();
            GameManifestJsonModel model;

            File minecraftMainJar;

            if (config.getLaunchConfig() == null) throw new ConfigCorruptException();
            // TODO load java environment
            {
                JavaEnvironment env = config.getLaunchConfig().getEnv();
                args.add(CommandArg.create(
                        env != null ?
                                env.getExecutable().getPath() :
                                "java") // fall back to default java in $PATH env var
                );
            }
            // TODO check and load manifest json
            {
                if (!checkIsValid()) throw new ManifestJsonCorruptException();
                model = manifestJson.readManifest();
            }
            // TODO check main jar and fetch path
            {
                minecraftMainJar = instancePath.resolve(instanceName + ".jar").toFile();
                if (!minecraftMainJar.exists()) throw new MainJarCorruptException();
            }
            // TODO check and load java arguments
            {
                if (model.getArguments() == null || model.getArguments().getJvmArguments() == null) {
                    args.addAll(
                            JsonUtil.createList(
                                    JVMArgument.FILE_ENCODING.parseMap(
                                            JsonUtil.createSingleMap("encoding", "UTF-8")
                                    ),
                                    // TODO to be done
                                    JVMArgument.MINECRAFT_CLIENT_JAR.parseMap(
                                            JsonUtil.createSingleMap("jar_path", minecraftMainJar)
                                    ),
                                    JVMArgument.UNLOCK_EXPERIMENTAL_OPTIONS,

                                    JVMArgument.USE_G1GC,
                                    JVMArgument.GC_YOUNG_SIZE_PERCENT.parseMap(
                                            JsonUtil.createSingleMap("percent", 20)
                                    ),
                                    JVMArgument.GC_RESERVE_SIZE_PERCENT.parseMap(
                                            JsonUtil.createSingleMap("percent", 20)
                                    ),
                                    JVMArgument.MAX_GC_PAUSE.parseMap(
                                            JsonUtil.createSingleMap("millis", 50)
                                    ),
                                    JVMArgument.HEAP_REGION_SIZE.parseMap(
                                            JsonUtil.createSingleMap("size", MemorySize.create("16m"))
                                    ),
                                    JVMArgument.ADAPTIVE_SIZE_POLICY,
                                    JVMArgument.STACK_TRACE_FAST_THROW,
                                    JVMArgument.DONT_COMPILE_HUGE_METHODS,

                                    JVMArgument.FML_IGNORE_INVAILD_CERTIFICATES.parseMap(
                                            JsonUtil.createSingleMap("enable", true)
                                    ),
                                    JVMArgument.FML_IGNORE_PATCH_DISCREPANCIES.parseMap(
                                            JsonUtil.createSingleMap("enable", true)
                                    ),
                                    JVMArgument.USE_CODEBASE_ONLY.parseMap(
                                            JsonUtil.createSingleMap("enable", true)
                                    ),
                                    JVMArgument.TRUST_URL_CODE_BASE.parseMap(
                                            JsonUtil.createSingleMap("enable", false)
                                    ),
                                    // TODO log4j2 bug fix
                                    JVMArgument.DISABLE_MSG_LOOPUPS.parseMap(
                                            JsonUtil.createSingleMap("enable", true)
                                    ),
                                    JVMArgument.INTEL_PERFORMANCE,
                                    // TODO to be done
                                    JVMArgument.JAVA_LIBRARY_PATH.parseMap(
                                            JsonUtil.createSingleMap("lib_path", "null")
                                    ),
                                    // TODO to be done
                                    JVMArgument.MINECRAFT_LAUNCHER_BRAND.parseMap(
                                            JsonUtil.createSingleMap("launcher_brand", "null")
                                    ),
                                    // TODO to be done
                                    JVMArgument.MINECRAFT_LAUNCHER_VERSION.parseMap(
                                            JsonUtil.createSingleMap("launcher_version", "null")
                                    ),
                                    JVMArgument.STDOUT_ENCODING.parseMap(
                                            JsonUtil.createSingleMap("encoding", "UTF-8")
                                    ),
                                    JVMArgument.STDERR_ENCODING.parseMap(
                                            JsonUtil.createSingleMap("encoding", "UTF-8")
                                    ),
                                    JVMArgument.CLASSPATH,
                                    // TODO to be done
                                    null
                            )
                    );
                } else {
                    Map<String, Object> metadata = new HashMap<String, Object>() {{
                        // TODO to be implemented
                        put("${natives_directory}", "null");
                        put("${launcher_name}", "null");
                        put("${launcher_version}", "null");
                        put("${classpath}", "null");
                    }};

                    args.addAll(
                            JsonUtil.createList(
                                    JVMArgument.FILE_ENCODING.parseMap(
                                            JsonUtil.createSingleMap("encoding", "UTF-8")
                                    ),
                                    // TODO to be done
                                    JVMArgument.MINECRAFT_CLIENT_JAR.parseMap(
                                            JsonUtil.createSingleMap("jar_path", minecraftMainJar)
                                    ),
                                    JVMArgument.UNLOCK_EXPERIMENTAL_OPTIONS,

                                    JVMArgument.USE_G1GC,
                                    JVMArgument.GC_YOUNG_SIZE_PERCENT.parseMap(
                                            JsonUtil.createSingleMap("percent", 20)
                                    ),
                                    JVMArgument.GC_RESERVE_SIZE_PERCENT.parseMap(
                                            JsonUtil.createSingleMap("percent", 20)
                                    ),
                                    JVMArgument.MAX_GC_PAUSE.parseMap(
                                            JsonUtil.createSingleMap("millis", 50)
                                    ),
                                    JVMArgument.HEAP_REGION_SIZE.parseMap(
                                            JsonUtil.createSingleMap("size", MemorySize.create("16m"))
                                    ),
                                    JVMArgument.ADAPTIVE_SIZE_POLICY,
                                    JVMArgument.STACK_TRACE_FAST_THROW,
                                    JVMArgument.DONT_COMPILE_HUGE_METHODS,

                                    JVMArgument.FML_IGNORE_INVAILD_CERTIFICATES.parseMap(
                                            JsonUtil.createSingleMap("enable", true)
                                    ),
                                    JVMArgument.FML_IGNORE_PATCH_DISCREPANCIES.parseMap(
                                            JsonUtil.createSingleMap("enable", true)
                                    ),
                                    JVMArgument.USE_CODEBASE_ONLY.parseMap(
                                            JsonUtil.createSingleMap("enable", true)
                                    ),
                                    JVMArgument.TRUST_URL_CODE_BASE.parseMap(
                                            JsonUtil.createSingleMap("enable", false)
                                    ),
                                    // TODO log4j2 bug fix
                                    JVMArgument.DISABLE_MSG_LOOPUPS.parseMap(
                                            JsonUtil.createSingleMap("enable", true)
                                    ),
                                    JVMArgument.STDOUT_ENCODING.parseMap(
                                            JsonUtil.createSingleMap("encoding", "UTF-8")
                                    ),
                                    JVMArgument.STDERR_ENCODING.parseMap(
                                            JsonUtil.createSingleMap("encoding", "UTF-8")
                                    )
                            )
                    );

                    model.getArguments().getJvmArguments().forEach(a -> {
                        if (a.valid()) args.addAll(
                                a.getValue().stream()
                                        .map(s -> JVMArgument.create(s).parseMap(metadata))
                                        .collect(Collectors.toList())
                        );
                    });
                }
            }

            args.forEach(System.out::println);
        }

        private GameAssetsIndexFileModel getAssetsIndex(GameManifestJsonModel model) throws FileNotFoundException {
            return GSON_PARSER.fromJson(
                    new FileReader(getAssetsIndexFile(model)),
                    GameAssetsIndexFileModel.class
            );
        }

        private File getAssetsIndexFile(GameManifestJsonModel model) {
            return repository.getAssetsDirectory()
                    .resolve("indexes")
                    .resolve(model.getAssets() + ".json")
                    .toFile();
        }

        protected Text getTaskName() {
            return translatable("core.game.instance.fetchArgs.text");
        }
    }
}
