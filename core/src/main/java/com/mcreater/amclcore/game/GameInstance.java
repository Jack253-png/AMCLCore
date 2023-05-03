package com.mcreater.amclcore.game;

import com.mcreater.amclcore.command.CommandArg;
import com.mcreater.amclcore.concurrent.task.AbstractAction;
import com.mcreater.amclcore.exceptions.launch.ConfigCorruptException;
import com.mcreater.amclcore.exceptions.launch.ManifestJsonCorruptException;
import com.mcreater.amclcore.exceptions.report.ExceptionReporter;
import com.mcreater.amclcore.i18n.Text;
import com.mcreater.amclcore.java.JavaEnvironment;
import com.mcreater.amclcore.model.config.ConfigMainModel;
import com.mcreater.amclcore.model.game.GameManifestJsonModel;
import com.mcreater.amclcore.model.game.assets.GameAssetsIndexFileModel;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.nio.file.Path;
import java.util.List;
import java.util.Vector;

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

            if (config.getLaunchConfig() == null) throw new ConfigCorruptException();
            // TODO load java environment
            {
                JavaEnvironment env = config.getLaunchConfig().getEnv();
                args.add(CommandArg.create(env != null ? env.getExecutable().getPath() : "java"));
            }
            // TODO check and load manifest json
            {
                if (!checkIsValid()) throw new ManifestJsonCorruptException();
                model = manifestJson.readManifest();
            }
            System.out.println(model);
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
