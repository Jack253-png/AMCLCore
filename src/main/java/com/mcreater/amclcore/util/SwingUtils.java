package com.mcreater.amclcore.util;

import com.mcreater.amclcore.concurrent.AbstractTask;
import com.mcreater.amclcore.util.platform.OperatingSystem;

import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

public class SwingUtils {
    private static final String[] linuxBrowsers = {
            "xdg-open",
            "google-chrome",
            "firefox",
            "microsoft-edge",
            "opera",
            "konqueror",
            "mozilla"
    };
    public enum BrowserState {
        SUCCESS,
        NOT_FOUND,
        INTERNAL_EXCEPTION
    }
    public static class BrowserOpenTask extends AbstractTask<BrowserState> {
        private final String url;
        public BrowserOpenTask(String url) {
            this.url = url;
        }
        public BrowserState call() {
            try {
                if (supportAction(Desktop.Action.BROWSE)) {
                    getDesktop().browse(URI.create(url));
                } else {
                    if (OperatingSystem.CURRENT_OS == OperatingSystem.WINDOWS) {
                        Runtime.getRuntime().exec(new String[]{"rundll32.exe", "url.dll,FileProtocolHandler", url});
                    } else if (OperatingSystem.CURRENT_OS == OperatingSystem.LINUX) {
                        for (String browser : linuxBrowsers) {
                            try (final InputStream is = Runtime.getRuntime().exec(new String[]{"which", browser}).getInputStream()) {
                                if (is.read() != -1) {
                                    Runtime.getRuntime().exec(new String[]{browser, url});
                                    return BrowserState.SUCCESS;
                                }
                            } catch (Throwable e) {
                                return BrowserState.INTERNAL_EXCEPTION;
                            }
                        }
                    } else if (OperatingSystem.CURRENT_OS == OperatingSystem.OSX) {
                        Runtime.getRuntime().exec(new String[]{"/usr/bin/open", url});
                    } else {
                        return BrowserState.INTERNAL_EXCEPTION;
                    }
                }
            } catch (IOException e) {
                return BrowserState.INTERNAL_EXCEPTION;
            }
            return BrowserState.NOT_FOUND;
        }
    }
    public static BrowserOpenTask openBrowser(String url) {
        return new BrowserOpenTask(url);
    }

    public static void copyContent(String content) {
        Toolkit.getDefaultToolkit()
                .getSystemClipboard()
                .setContents(new StringSelection(content), (clipboard1, transferable) -> {});
    }


    private static boolean supportAction(Desktop.Action action) {
        return getDesktop().isSupported(action);
    }

    private static Desktop getDesktop() {
        return Desktop.getDesktop();
    }
}
