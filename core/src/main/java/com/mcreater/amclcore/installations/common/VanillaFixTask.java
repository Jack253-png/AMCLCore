package com.mcreater.amclcore.installations.common;

import com.mcreater.amclcore.concurrent.AdvancedForkJoinPool;
import com.mcreater.amclcore.concurrent.ConcurrentExecutors;
import com.mcreater.amclcore.concurrent.task.AbstractAction;
import com.mcreater.amclcore.concurrent.task.AbstractTask;
import com.mcreater.amclcore.game.GameManifestJson;
import com.mcreater.amclcore.game.GameRepository;
import com.mcreater.amclcore.i18n.Text;
import com.mcreater.amclcore.model.game.GameManifestJsonModel;
import com.mcreater.amclcore.model.game.lib.GameDependedLibModel;
import com.mcreater.amclcore.util.hash.Sha1String;
import com.mcreater.amclcore.util.platform.Architecture;
import com.mcreater.amclcore.util.platform.Mapping;
import com.mcreater.amclcore.util.platform.OperatingSystem;
import com.mcreater.amclcore.util.url.DownloadTask;
import com.mcreater.amclcore.util.url.MinecraftMirroredResourceURL;
import lombok.AllArgsConstructor;

import java.nio.file.Path;
import java.util.List;
import java.util.Vector;

import static com.mcreater.amclcore.concurrent.ConcurrentExecutors.getExcHandler;
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

        AdvancedForkJoinPool dlPool = new AdvancedForkJoinPool(
                128,
                ConcurrentExecutors.ForkJoinWorkerThreadFactoryImpl.getINSTANCE(),
                getExcHandler(),
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

        tasks.add(new DownloadTask(
                manifest.getDownloads().getClient().getUrl().toSource(server),
                json.toInstance().getInstancePath().resolve(json.getName() + ".jar").toFile(),
                manifest.getDownloads().getClient().getSha1()
        ));

        manifest.getLibraries().parallelStream()
                .filter(GameDependedLibModel::hasNatives)
                .filter(GameDependedLibModel::valid)
                .filter(a -> a.isNormalLib() || a.getName().getPlatform().equals(Mapping.getNativeName()))
                .forEachOrdered(lib -> {
                    Path jarp;
                    MinecraftMirroredResourceURL url;
                    Sha1String sha1;
                    if (lib.isNormalLib()) {
                        String nativeId = lib
                                .getNatives()
                                .get(OperatingSystem.CURRENT_OS.getCheckedName())
                                .replace("${arch}", Architecture.CURRENT_ARCH.getBits().getBit());

                        jarp = lib.getDownloads()
                                .getClassifiers()
                                .get(nativeId)
                                .getJarPath();

                        url = lib.getDownloads()
                                .getClassifiers()
                                .get(nativeId)
                                .getUrl();

                        sha1 = lib.getDownloads()
                                .getClassifiers()
                                .get(nativeId)
                                .getSha1();
                    } else {
                        jarp = lib.getJarPath();
                        url = lib.getUrl();
                        sha1 = lib.getDownloads().getArtifact().getSha1();
                    }

                    jarp = repository.getLibrariesDirectory().resolve(jarp);
                    url = url.toSource(server);
                    System.out.printf("%s\n%s\n%s\n", jarp, url, sha1);
                });

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
