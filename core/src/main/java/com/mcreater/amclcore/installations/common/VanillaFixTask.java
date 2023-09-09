package com.mcreater.amclcore.installations.common;

import com.mcreater.amclcore.concurrent.ConcurrentExecutors;
import com.mcreater.amclcore.concurrent.ExtendForkJoinPool;
import com.mcreater.amclcore.concurrent.task.AbstractAction;
import com.mcreater.amclcore.concurrent.task.AbstractTask;
import com.mcreater.amclcore.game.GameManifestJson;
import com.mcreater.amclcore.game.GameRepository;
import com.mcreater.amclcore.i18n.Text;
import com.mcreater.amclcore.model.game.GameManifestJsonModel;
import com.mcreater.amclcore.model.game.lib.GameDependedLibModel;
import com.mcreater.amclcore.util.url.DownloadTask;
import com.mcreater.amclcore.util.url.MinecraftMirroredResourceURL;
import lombok.AllArgsConstructor;

import java.util.List;
import java.util.Vector;

import static com.mcreater.amclcore.concurrent.ConcurrentExecutors.excHandler;
import static com.mcreater.amclcore.i18n.I18NManager.translatable;
import static com.mcreater.amclcore.util.url.DownloadTask.vaildate;

@AllArgsConstructor
public class VanillaFixTask extends AbstractAction {
    private GameRepository repository;
    private GameManifestJson json;
    private MinecraftMirroredResourceURL.MirrorServer server;

    protected void execute() throws Exception {
        GameManifestJsonModel manifest = json.readManifest();
        new DownloadTask(manifest.getAssetIndex().getUrl().toSource(server), repository.getAssetIndexDirectory().resolve(manifest.getAssets() + ".json").toFile(), manifest.getAssetIndex().getSha1())
                .bindTo(this)
                .get();

        ExtendForkJoinPool dlPool = new ExtendForkJoinPool(
                256,
                ConcurrentExecutors.ForkJoinWorkerThreadFactoryImpl.INSTANCE,
                excHandler,
                true
        );

        List<AbstractTask<?>> tasks = new Vector<>();
        json.toInstance().getAssetsIndex(manifest).getObjects().values().parallelStream()
                .filter(a -> vaildate(repository.getAssetFilePath(a.getHash()).toFile(), a.getHash()))
                .map(m -> new DownloadTask(
                        MinecraftMirroredResourceURL.ASSETS(m.getHash().getRaw()).toSource(server),
                        repository.getAssetFilePath(m.getHash()).toFile(),
                        m.getHash()
                ))
                .forEach(tasks::add);

        manifest.getLibraries().parallelStream()
                .filter(GameDependedLibModel::isNormalLib)
                .filter(a -> vaildate(repository.getLibrariesDirectory().resolve(a.getJarPath()).toFile(), a.getHash()))
                .map(m -> new DownloadTask(
                        m.getUrl().toSource(server),
                        repository.getLibrariesDirectory().resolve(m.getJarPath()).toFile(),
                        m.getHash()
                ))
                .forEach(tasks::add);


        tasks.forEach(abstractTask -> abstractTask.submitTo(dlPool));
        do {
            System.out.println(dlPool.getActiveThreadCount());
            Thread.sleep(1000);
        }
        while (dlPool.getActiveThreadCount() != 0);
    }

    protected Text getTaskName() {
        return translatable("core.install.task.fixresources.name");
    }
}
