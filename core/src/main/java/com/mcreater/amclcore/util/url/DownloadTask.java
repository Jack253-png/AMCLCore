package com.mcreater.amclcore.util.url;

import com.mcreater.amclcore.concurrent.TaskState;
import com.mcreater.amclcore.concurrent.task.AbstractAction;
import com.mcreater.amclcore.i18n.Text;
import com.mcreater.amclcore.util.HttpClientWrapper;
import com.mcreater.amclcore.util.hash.Sha1String;
import lombok.AllArgsConstructor;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;

import static com.mcreater.amclcore.i18n.I18NManager.fixed;
import static com.mcreater.amclcore.i18n.I18NManager.translatable;

@AllArgsConstructor
public class DownloadTask extends AbstractAction {
    private MinecraftMirroredResourceURL url;
    private File local;
    private Sha1String sha1String;

    public DownloadTask(MinecraftMirroredResourceURL url, File local) {
        this(url, local, null);
    }

    protected void execute() throws Exception {
        setState(
                TaskState.<Void>builder()
                        .totalStage(3)
                        .currentStage(0)
                        .message(fixed(""))
                        .taskType(TaskState.Type.EXECUTING)
                        .build()
        );

        setState(
                TaskState.<Void>builder()
                        .totalStage(3)
                        .currentStage(1)
                        .message(translatable("core.download.checkhash"))
                        .taskType(TaskState.Type.EXECUTING)
                        .build()
        );
        boolean needDownload = true;
        if (sha1String != null && local.exists()) needDownload = !sha1String.validate(local);
        if (!needDownload) return;

        if (!local.exists()) {
            local.getParentFile().mkdirs();
            local.createNewFile();
        }

        setState(
                TaskState.<Void>builder()
                        .totalStage(3)
                        .currentStage(2)
                        .message(translatable("core.download.downloading"))
                        .taskType(TaskState.Type.EXECUTING)
                        .build()
        );
        HttpClientWrapper.create(HttpClientWrapper.Method.GET)
                .uriScheme(url.toDownloadFormatWithMirror().getLeft())
                .uri(url.toDownloadFormatWithMirror().getRight())
                .reqTimeout(5000)
                .socTimeout(5000)
                .send()
                .writeTo(Files.newOutputStream(local.toPath(), StandardOpenOption.WRITE));

        setState(
                TaskState.<Void>builder()
                        .totalStage(3)
                        .currentStage(3)
                        .message(translatable("core.download.complete"))
                        .taskType(TaskState.Type.EXECUTING)
                        .build()
        );
    }

    protected Text getTaskName() {
        return fixed(String.format("%s -> %s", url, local));
    }
}
