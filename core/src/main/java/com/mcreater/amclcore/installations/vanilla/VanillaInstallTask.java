package com.mcreater.amclcore.installations.vanilla;

import com.mcreater.amclcore.concurrent.task.AbstractAction;
import com.mcreater.amclcore.exceptions.installation.MinecraftVersionNotFountException;
import com.mcreater.amclcore.game.GameManifestJson;
import com.mcreater.amclcore.game.GameRepository;
import com.mcreater.amclcore.i18n.I18NManager;
import com.mcreater.amclcore.i18n.Text;
import com.mcreater.amclcore.installations.common.LauncherMetadataFetchTask;
import com.mcreater.amclcore.model.game.GameManifestJsonModel;
import com.mcreater.amclcore.model.installation.launchermeta.LauncherMetaModel;
import com.mcreater.amclcore.model.installation.launchermeta.LauncherMetaVersionModel;
import com.mcreater.amclcore.util.url.DownloadTask;
import lombok.AllArgsConstructor;

import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@AllArgsConstructor
public class VanillaInstallTask extends AbstractAction {
    private GameRepository repository;
    private String id;
    private String installName;

    protected void execute() throws Exception {
        LauncherMetaModel launchermeta = new LauncherMetadataFetchTask()
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

        new DownloadTask(versionModel.get().getUrl(), json.getJsonPath().toFile(), versionModel.get().getSha1())
                .bindTo(this)
                .get();

        GameManifestJsonModel manifest = json.readManifest();

        new DownloadTask(manifest.getAssetIndex().getUrl(), repository.getAssetIndexDirectory().resolve(manifest.getAssets() + ".json").toFile(), manifest.getAssetIndex().getSha1())
                .bindTo(this)
                .get();
    }

    protected Text getTaskName() {
        return I18NManager.translatable("core.install.vanilla");
    }
}
