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

    public static boolean vaildate(File file, Sha1String sha1String) {
        return !file.exists() || !sha1String.validate(file);
    }

    protected void execute() throws Exception {
        setState(
                new TaskState<>(
                        TaskState.Type.EXECUTING,
                        null,
                        null,
                        3, 0, fixed("")
                )
        );

        setState(
                new TaskState<>(
                        TaskState.Type.EXECUTING,
                        null,
                        null,
                        3, 1, translatable("core.download.checkhash")
                )
        );
        boolean needDownload = true;
        if (sha1String != null && local.exists()) needDownload = !sha1String.validate(local);
        if (!needDownload) return;

        if (local.exists()) local.delete();
        local.getParentFile().mkdirs();
        local.createNewFile();

        setState(
                new TaskState<>(
                        TaskState.Type.EXECUTING,
                        null,
                        null,
                        3, 2, translatable("core.download.downloading")
                )
        );

        while (true) {
            try {
                HttpClientWrapper.create(HttpClientWrapper.Method.GET)
                        .uriScheme(url.toDownloadFormatWithMirror().getLeft())
                        .uri(url.toDownloadFormatWithMirror().getRight())
                        .reqTimeout(5000)
                        .socTimeout(5000)
                        .retry(5)
                        .send()
                        .writeTo(Files.newOutputStream(local.toPath(), StandardOpenOption.WRITE));
                break;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        setState(
                new TaskState<>(
                        TaskState.Type.EXECUTING,
                        null,
                        null,
                        3, 3, translatable("core.download.complete")
                )
        );
    }

    protected Text getTaskName() {
        return fixed(String.format("%s -> %s", url, local));
    }
}
