package com.mcreater.amclcore.installations.common;

import com.mcreater.amclcore.concurrent.task.AbstractTask;
import com.mcreater.amclcore.i18n.Text;
import com.mcreater.amclcore.model.installation.launchermeta.LauncherMetaModel;
import com.mcreater.amclcore.util.NetUtil;
import com.mcreater.amclcore.util.url.MinecraftMirroredResourceURL;
import lombok.AllArgsConstructor;

import static com.mcreater.amclcore.i18n.I18NManager.translatable;

@AllArgsConstructor
public class LauncherMetadataFetchTask extends AbstractTask<LauncherMetaModel> {
    private MinecraftMirroredResourceURL.MirrorServer server;

    protected LauncherMetaModel call() throws Exception {
        return NetUtil.readFrom(MinecraftMirroredResourceURL.MANIFEST_V2.toSource(server), LauncherMetaModel.class);
    }

    protected Text getTaskName() {
        return translatable("core.installation.task.fetch_metadata.name");
    }
}
