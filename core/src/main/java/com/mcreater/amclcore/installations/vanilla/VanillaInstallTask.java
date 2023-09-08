package com.mcreater.amclcore.installations.vanilla;

import com.mcreater.amclcore.concurrent.task.AbstractAction;
import com.mcreater.amclcore.i18n.I18NManager;
import com.mcreater.amclcore.i18n.Text;
import com.mcreater.amclcore.util.NetUtil;
import com.mcreater.amclcore.util.url.MinecraftMirroredResourceURL;

public class VanillaInstallTask extends AbstractAction {
    protected void execute() throws Exception {
        System.out.println(NetUtil.readFrom(MinecraftMirroredResourceURL.MANIFEST_V2));
    }

    protected Text getTaskName() {
        return I18NManager.translatable("core.install.vanilla");
    }
}
