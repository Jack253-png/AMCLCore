package com.mcreater.amclcore.installations.vanilla;

import com.mcreater.amclcore.concurrent.TaskState;
import com.mcreater.amclcore.concurrent.task.AbstractAction;
import com.mcreater.amclcore.exceptions.installation.MinecraftVersionNotFountException;
import com.mcreater.amclcore.game.GameManifestJson;
import com.mcreater.amclcore.game.GameRepository;
import com.mcreater.amclcore.i18n.Text;
import com.mcreater.amclcore.installations.common.LauncherMetadataFetchTask;
import com.mcreater.amclcore.installations.common.VanillaFixTask;
import com.mcreater.amclcore.model.installation.launchermeta.LauncherMetaModel;
import com.mcreater.amclcore.model.installation.launchermeta.LauncherMetaVersionModel;
import com.mcreater.amclcore.util.platform.Mapping;
import com.mcreater.amclcore.util.url.DownloadTask;
import com.mcreater.amclcore.util.url.MinecraftMirroredResourceURL;
import lombok.AllArgsConstructor;

import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static com.mcreater.amclcore.i18n.I18NManager.fixed;
import static com.mcreater.amclcore.i18n.I18NManager.translatable;

@AllArgsConstructor
public class VanillaInstallTask extends AbstractAction {
    private GameRepository repository;
    private String id;
    private String installName;
    private MinecraftMirroredResourceURL.MirrorServer server;

    protected void execute() throws Exception {
        setState(TaskState.<Void>builder()
                .message(translatable("core.install.vanilla.versionjson"))
                .totalStage(2)
                .currentStage(0)
                .build()
        );
        LauncherMetaModel launchermeta = new LauncherMetadataFetchTask(server)
                .bindTo(this)
                .get()
                .orElse(null);

        AtomicReference<LauncherMetaVersionModel> versionModel = new AtomicReference<>();
        if (!launchermeta.getVersions().stream().map(LauncherMetaVersionModel::getId).collect(Collectors.toList()).contains(id))
            throw new MinecraftVersionNotFountException();
        launchermeta.getVersions()
                .stream()
                .filter(launcherMetaVersionModel -> launcherMetaVersionModel.getId().equals(id))
                .findFirst()
                .ifPresent(versionModel::set);

        GameManifestJson json = GameManifestJson.create(repository, installName);
        new DownloadTask(versionModel.get().getUrl().toSource(server), json.getJsonPath().toFile(), versionModel.get().getSha1())
                .bindTo(this)
                .get();
        Mapping.patchVersion(json);

        setState(TaskState.<Void>builder()
                .message(translatable("core.install.vanilla.fixresources"))
                .totalStage(2)
                .currentStage(1)
                .build()
        );

        new VanillaFixTask(repository, json, server)
                .bindTo(this)
                .get();

        setState(TaskState.<Void>builder()
                .message(fixed(""))
                .totalStage(2)
                .currentStage(2)
                .build()
        );
    }

    protected Text getTaskName() {
        return translatable("core.install.vanilla");
    }
}
