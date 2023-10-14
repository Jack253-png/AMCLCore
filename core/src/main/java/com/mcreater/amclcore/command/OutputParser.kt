package com.mcreater.amclcore.command

import com.mcreater.amclcore.MetaData.Companion.isUseAnsiOutputOverride
import com.mcreater.amclcore.command.ansi.ColorTheme
import com.mcreater.amclcore.command.ansi.DefaultTheme
import org.fusesource.jansi.Ansi
import java.io.PrintStream
import java.util.regex.Matcher
import java.util.regex.Pattern


class OutputParser {
    companion object {
        var colorTheme: ColorTheme = DefaultTheme()
        private val MC_DEFAULT_FORMAT = Pattern.compile("\\[(?<time>.*)] \\[(?<thread>.*)/(?<type>.*)]: (?<message>.*)")
        private val MC_DEFAULT_FORMAT2 =
            Pattern.compile("\\[(?<time>.*)] \\[(?<thread>.*)/(?<type>.*)] \\[(?<loc>.*)/(?<part>.*)]: (?<message>.*)")
        private val JAVA_EXC_MAIN =
            Pattern.compile("Exception in thread \"(?<thread>.*)\" (?<excname>.*): (?<message>.*)")
        private val JAVA_EXC_NAME = Pattern.compile("(?<excname>.*): (?<message>.*)")
        private val JAVA_EXC_STACK =
            Pattern.compile("\tat (?<method>.*)((?<source>.*):(?<line>.*)) ~\\[(?<jar>.*):(?<moudle>.*)]")
        private val JAVA_EXC_STACK2 =
            Pattern.compile("\tat (?<method>.*)((?<source>.*):(?<line>.*)) \\[(?<jar>.*):(?<moudle>.*)]")
        private val JAVA_EXC_STACK_BASE = Pattern.compile("\tat (?<stack>.*)")
        private val JAVA_EXC_END = Pattern.compile("\t\\.\\.\\. (?<stacks>.*) more")

        enum class OutputType {
            STDOUT,
            STDERR
        }

        enum class LogType {
            FATAL,
            ERROR,
            WARN,
            INFO,
            DEBUG,
            TRACE
        }
    }

    class OutputLine private constructor(
        private val data: String? = null,
        private val type: OutputType? = null
    ) {
        val logType: LogType
            get() {
                val matcher = parse()
                if (!matcher.find()) {
                    return if (JAVA_EXC_MAIN.matcher(data!!).find() ||
                        JAVA_EXC_NAME.matcher(data).find() ||
                        JAVA_EXC_STACK.matcher(data).find() ||
                        JAVA_EXC_STACK2.matcher(data).find() ||
                        JAVA_EXC_STACK_BASE.matcher(data).find() ||
                        JAVA_EXC_END.matcher(data).find()
                    ) {
                        LogType.ERROR
                    } else {
                        if (type == OutputType.STDERR) LogType.WARN else LogType.INFO
                    }
                }
                val logType = matcher.group("type")
                return when (type) {
                    OutputType.STDOUT -> when (logType.lowercase()) {
                        "fatal" -> LogType.FATAL
                        "error" -> LogType.ERROR
                        "warn" -> LogType.WARN
                        "info" -> LogType.INFO
                        "debug" -> LogType.DEBUG
                        "trace" -> LogType.TRACE
                        else -> LogType.INFO
                    }

                    OutputType.STDERR -> when (logType.lowercase()) {
                        "fatal" -> LogType.FATAL
                        "error" -> LogType.ERROR
                        "warn" -> LogType.WARN
                        else -> LogType.WARN
                    }

                    else -> when (logType.lowercase()) {
                        "fatal" -> LogType.FATAL
                        "error" -> LogType.ERROR
                        "warn" -> LogType.WARN
                        "info" -> LogType.INFO
                        "debug" -> LogType.DEBUG
                        "trace" -> LogType.TRACE
                        else -> LogType.INFO
                    }
                }
            }

        fun printAnsi() {
            print("\r")
            if (!isUseAnsiOutputOverride()) {
                println(data)
                return
            }
            val ansi = Ansi.ansi()
            val c: Ansi.Consumer? = when (logType) {
                LogType.FATAL -> colorTheme.applyFatal()
                LogType.ERROR -> colorTheme.applyError()
                LogType.WARN -> colorTheme.applyWarning()
                LogType.INFO -> colorTheme.applyInfo()
                LogType.DEBUG -> colorTheme.applyDebug()
                LogType.TRACE -> colorTheme.applyTrace()
            }
            println(ansi.apply(c).a(data).eraseLine())
        }

        private fun parse(): Matcher {
            return if (MC_DEFAULT_FORMAT2.matcher(data!!)
                    .find()
            ) MC_DEFAULT_FORMAT2.matcher(data) else MC_DEFAULT_FORMAT.matcher(data)
        }

        companion object {
            @JvmStatic
            fun create(data: String?, type: OutputType?): OutputLine {
                return OutputLine(data, type)
            }

            @JvmStatic
            fun create(data: String?, stream: PrintStream): OutputLine {
                return OutputLine(data, if (stream === System.err) OutputType.STDERR else OutputType.STDOUT)
            }
        }
    }
}
