package com.mcreater.amclcore.game;

import com.mcreater.amclcore.account.AbstractAccount;
import com.mcreater.amclcore.command.CommandArg;
import com.mcreater.amclcore.command.StartProcessTask;
import com.mcreater.amclcore.concurrent.TaskState;
import com.mcreater.amclcore.concurrent.task.AbstractAction;
import com.mcreater.amclcore.concurrent.task.AbstractTask;
import com.mcreater.amclcore.exceptions.launch.AccountNotSelectedException;
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
import com.mcreater.amclcore.model.game.arguments.GameArgumentsModel;
import com.mcreater.amclcore.model.game.assets.GameAssetsIndexFileModel;
import com.mcreater.amclcore.model.game.lib.GameDependedLibModel;
import com.mcreater.amclcore.model.game.rule.GameRuleFeatureModel;
import com.mcreater.amclcore.util.JsonUtil;
import com.mcreater.amclcore.util.platform.OperatingSystem;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.mcreater.amclcore.MetaData.*;
import static com.mcreater.amclcore.i18n.I18NManager.translatable;
import static com.mcreater.amclcore.util.JsonUtil.GSON_PARSER;
import static com.mcreater.amclcore.util.StringUtil.toNoLineUUID;

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

    public FetchLaunchArgsTask fetchLaunchArgsAsync(ConfigMainModel config) {
        return new FetchLaunchArgsTask(config);
    }

    public LaunchTask launchAsync(ConfigMainModel config) {
        return new LaunchTask(config);
    }

    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public class LaunchTask extends AbstractAction {
        private ConfigMainModel config;

        protected void execute() throws Exception {
            List<CommandArg> command = fetchLaunchArgsAsync(config)
                    .bindTo(this)
                    .get()
                    .orElseThrow(NullPointerException::new);

            StartProcessTask.create(command)
                    .setStartPath(getGameDir(config.getLaunchConfig().isUseSelfGamePath()))
                    .bindTo(this)
                    .get();
        }

        protected Text getTaskName() {
            return translatable("core.game.instance.task.launch.name");
        }
    }

    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public class FetchLaunchArgsTask extends AbstractTask<List<CommandArg>> {
        private ConfigMainModel config;

        protected List<CommandArg> call() throws Exception {
            setState(
                    TaskState.<List<CommandArg>>builder()
                            .currentStage(0)
                            .totalStage(1)
                            .message(translatable("core.game.instance.launchAsync.pre_launching"))
                            .build()
            );
            final Optional<String> nameOverride = Optional.ofNullable(config.getLaunchConfig().getLauncherNameOverride());
            final Optional<String> versionOverride = Optional.ofNullable(config.getLaunchConfig().getLauncherVersionOverride());

            List<CommandArg> args = new Vector<>();
            GameManifestJsonModel model;

            File minecraftMainJar;
            Path libPath = GameInstance.this.repository.getLibrariesDirectory();
            String classpath;
            List<Path> nativeLibs;

            Optional<AbstractAccount> account = config.getSelectedAccount();
            if (!account.isPresent()) throw new AccountNotSelectedException();

            Path gameDir = getGameDir(config.getLaunchConfig().isUseSelfGamePath());
            Path nativePath = instancePath.resolve(instanceName + "-natives");

            Map<String, Object> gameArgMetaData;

            GameRuleFeatureModel features = GameRuleFeatureModel.builder()
                    .hasCustomResolution(true)
                    .build();

            if (config.getLaunchConfig() == null) throw new ConfigCorruptException();
            // TODO load java environment
            {
                args.add(CommandArg.create(
                                config.getLaunchConfig().getEnv()
                                        .map(JavaEnvironment::getExecutable)
                                        .map(File::getPath)
                                        .orElse("java") // fall back to java in $PATH env var
                        )
                );
            }
            // TODO check and load manifest json
            {
                if (!checkIsValid()) throw new ManifestJsonCorruptException();
                model = manifestJson.readManifest();

                gameArgMetaData = new HashMap<String, Object>() {{
                    // show rule: F3 debug -> {launcher_brand}/{version_name}/{version_type}
                    put("version_name", instanceName);
                    put("version_type", getLauncherFullName());
                    put("resolution_width", 854);
                    put("resolution_height", 480);
                    put("auth_player_name", account.get().getAccountName());
                    put("auth_access_token", account.get().getAccessToken());
                    put("auth_uuid", toNoLineUUID(account.get().getUuid()));
                    put("user_type", AbstractAccount.UserType.MOJANG);
                    put("game_directory", gameDir);
                    put("assets_root", repository.getAssetsDirectory());
                    put("game_assets", gameDir.resolve("resources"));
                    put("assets_index_name", model.getAssets());
                }};
            }
            // TODO check main jar and fetch path
            {
                minecraftMainJar = instancePath.resolve(instanceName + ".jar").toFile();
                if (!minecraftMainJar.exists()) throw new MainJarCorruptException();
            }
            // TODO check and load libs
            {
                classpath = Stream.concat(
                                model.getLibraries().stream()
                                        .filter(GameDependedLibModel::valid)
                                        .filter(GameDependedLibModel::isNormalLib)
                                        .map(GameDependedLibModel::getJarPath)
                                        .map(libPath::resolve),
                                Stream.of(minecraftMainJar.toPath())
                        )
                        .map(Path::toString)
                        .distinct()
                        .collect(Collectors.joining(OperatingSystem.PATH_SEPARATOR));

                nativeLibs = model.getLibraries().stream()
                        .filter(GameDependedLibModel::valid)
                        .filter(GameDependedLibModel::hasNatives)
                        .map(lib -> {
                            if (lib.isNormalLib()) {
                                String nativeId = lib.getNatives().get(OperatingSystem.CURRENT_OS.getCheckedName());
                                return lib.getDownloads()
                                        .getClassifiers()
                                        .get(nativeId)
                                        .getJarPath();
                            } else return lib.getJarPath();
                        })
                        .map(libPath::resolve)
                        .map(Path::toString)
                        .distinct()
                        .map(Paths::get)
                        .collect(Collectors.toList());
            }
            // TODO check and load java arguments
            {
                // version < 1.14
                if (model.getArguments() == null || model.getArguments().getJvmArguments() == null) {
                    args.addAll(
                            JsonUtil.createList(
                                    JVMArgument.FILE_ENCODING.parseMap(
                                            JsonUtil.createSingleMap("encoding", "UTF-8")
                                    ),
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
                                            JsonUtil.createSingleMap("size",
                                                    MemorySize.create(16, MemorySize.MemoryUnit.MEGABYTES)
                                            )
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
                                            JsonUtil.createSingleMap("lib_path", nativePath)
                                    ),
                                    JVMArgument.MINECRAFT_LAUNCHER_BRAND.parseMap(
                                            JsonUtil.createSingleMap("launcher_brand", nameOverride.orElse(getLauncherName()))
                                    ),
                                    JVMArgument.MINECRAFT_LAUNCHER_VERSION.parseMap(
                                            JsonUtil.createSingleMap("launcher_version", versionOverride.orElse(getLauncherFullVersion()))
                                    ),
                                    JVMArgument.STDOUT_ENCODING.parseMap(
                                            JsonUtil.createSingleMap("encoding", "UTF-8")
                                    ),
                                    JVMArgument.STDERR_ENCODING.parseMap(
                                            JsonUtil.createSingleMap("encoding", "UTF-8")
                                    ),
                                    JVMArgument.CLASSPATH,
                                    CommandArg.create(classpath),
                                    JVMArgument.MAX_HEAP_SIZE.parseMap(
                                            JsonUtil.createSingleMap("size", config.getLaunchConfig().getMemory().getMaxMemory())
                                    ),
                                    JVMArgument.MIN_HEAP_SIZE.parseMap(
                                            JsonUtil.createSingleMap("size", config.getLaunchConfig().getMemory().getMinMemory())
                                    )
                            )
                    );
                } else {
                    Map<String, Object> metadata = new HashMap<String, Object>() {{
                        // TODO to be implemented
                        put("natives_directory", nativePath);
                        put("launcher_name", nameOverride.orElse(getLauncherName()));
                        put("launcher_version", versionOverride.orElse(getLauncherFullVersion()));
                        put("version_name", instanceName);
                        put("library_directory", libPath);
                        put("classpath_separator", OperatingSystem.PATH_SEPARATOR);
                        put("classpath", CommandArg.create(classpath));
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
                                            JsonUtil.createSingleMap("size",
                                                    MemorySize.create(16, MemorySize.MemoryUnit.MEGABYTES)
                                            )
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

                    model.getArguments().getJvmArguments().stream()
                            .filter(GameArgumentsModel.GameArgumentsItem::valid)
                            .map(GameArgumentsModel.GameArgumentsItem::getValue)
                            .flatMap(Collection::stream)
                            .map(JVMArgument::create)
                            .map(jvmArgument -> jvmArgument.parseMap(metadata))
                            .forEach(args::add);

                    args.addAll(
                            JsonUtil.createList(
                                    JVMArgument.MAX_HEAP_SIZE.parseMap(
                                            JsonUtil.createSingleMap("size", config.getLaunchConfig().getMemory().getMaxMemory())
                                    ),
                                    JVMArgument.MIN_HEAP_SIZE.parseMap(
                                            JsonUtil.createSingleMap("size", config.getLaunchConfig().getMemory().getMinMemory())
                                    )
                            )
                    );
                }
            }
            // TODO load main class
            {
                args.add(CommandArg.create(model.getMainClass()));
            }
            // TODO load game args
            {
                if (model.getArguments() != null && model.getArguments().getGameArguments() != null) {
                    model.getArguments().getGameArguments().stream()
                            .filter(i -> i.valid(features))
                            .map(GameArgumentsModel.GameArgumentsItem::getValue)
                            .flatMap(Collection::stream)
                            .map(CommandArg::create)
                            .map(a -> a.parseMap(gameArgMetaData))
                            .forEach(args::add);
                } else if (model.getMinecraftArguments() != null) {
                    Arrays.stream(model.getMinecraftArguments().split(" "))
                            .map(CommandArg::create)
                            .map(a -> a.parseMap(gameArgMetaData))
                            .forEach(args::add);
                }
            }

            return args;
        }

        protected Text getTaskName() {
            return translatable("core.game.instance.fetchArgs.text");
        }
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

    private Path getGameDir(boolean useSelfDic) {
        return useSelfDic ? instancePath : repository.getPath();
    }
}
