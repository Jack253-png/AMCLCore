package com.mcreater.amclcore.installations.common;

import com.mcreater.amclcore.concurrent.task.AbstractTask;
import com.mcreater.amclcore.i18n.Text;

import static com.mcreater.amclcore.i18n.I18NManager.translatable;

public class LauncherMetadataFetchTask extends AbstractTask<Void> {
    protected Void call() throws Exception {
        return null;
    }

    protected Text getTaskName() {
        return translatable("core.installation.task.fetch_metadata.name");
    }
}
