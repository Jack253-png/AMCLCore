package com.mcreater.amclcore.util

import com.mcreater.amclcore.concurrent.task.AbstractAction
import com.mcreater.amclcore.concurrent.task.AbstractTask
import com.mcreater.amclcore.i18n.I18NManager.Companion.translatable
import com.mcreater.amclcore.i18n.Text
import com.mcreater.amclcore.util.platform.OperatingSystem
import java.awt.Desktop
import java.awt.Toolkit
import java.awt.datatransfer.Clipboard
import java.awt.datatransfer.StringSelection
import java.awt.datatransfer.Transferable
import java.io.IOException
import java.net.URI


class SwingUtil {
    companion object {
        @JvmStatic
        private val linuxBrowsers = arrayOf(
            "xdg-open",
            "google-chrome",
            "firefox",
            "microsoft-edge",
            "opera",
            "konqueror",
            "mozilla"
        )

        @JvmStatic
        fun openBrowserAsync(url: String): BrowserOpenTask {
            return BrowserOpenTask(url)
        }

        @JvmStatic
        fun copyContentAsync(content: String): CopyContentTask {
            return CopyContentTask(content)
        }

        @JvmStatic
        private fun supportAction(action: Desktop.Action): Boolean {
            return desktop.isSupported(action)
        }

        @JvmStatic
        private val desktop: Desktop
            get() = Desktop.getDesktop()
    }

    enum class BrowserState {
        SUCCESS,
        NOT_FOUND,
        INTERNAL_EXCEPTION
    }

    class BrowserOpenTask(private val url: String) :
        AbstractTask<BrowserState?>() {
        override fun call(): BrowserState {
            try {
                if (supportAction(Desktop.Action.BROWSE)) {
                    desktop.browse(URI.create(url))
                } else {
                    when (OperatingSystem.CURRENT_OS) {
                        OperatingSystem.WINDOWS -> {
                            Runtime.getRuntime().exec(
                                arrayOf(
                                    "rundll32.exe", "url.dll,FileProtocolHandler",
                                    url
                                )
                            )
                        }

                        OperatingSystem.LINUX -> {
                            for (browser in linuxBrowsers) {
                                try {
                                    Runtime.getRuntime().exec(arrayOf("which", browser)).inputStream.use { `is` ->
                                        if (`is`.read() != -1) {
                                            Runtime.getRuntime().exec(arrayOf(browser, url))
                                            return BrowserState.SUCCESS
                                        }
                                    }
                                } catch (e: Throwable) {
                                    return BrowserState.INTERNAL_EXCEPTION
                                }
                            }
                        }

                        OperatingSystem.OSX -> {
                            Runtime.getRuntime().exec(arrayOf("/usr/bin/open", url))
                        }

                        else -> {
                            return BrowserState.INTERNAL_EXCEPTION
                        }
                    }
                }
            } catch (e: IOException) {
                return BrowserState.INTERNAL_EXCEPTION
            }
            return BrowserState.NOT_FOUND
        }

        override fun getTaskName(): Text {
            return translatable("core.swing.api.task.browser.name", StringUtil.shortString(url, 20))
        }
    }

    class CopyContentTask(private val content: String) : AbstractAction() {
        override fun execute() {
            Toolkit.getDefaultToolkit()
                .systemClipboard
                .setContents(StringSelection(content)) { _: Clipboard?, _: Transferable? -> }
        }

        override fun getTaskName(): Text {
            return translatable(
                "core.swing.api.task.clipboard.name", StringUtil.shortString(
                    content, 20
                )
            )
        }
    }
}
