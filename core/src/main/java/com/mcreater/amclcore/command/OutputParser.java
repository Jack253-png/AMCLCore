package com.mcreater.amclcore.command;

import com.mcreater.amclcore.command.ansi.ColorTheme;
import com.mcreater.amclcore.command.ansi.DefaultTheme;
import lombok.*;
import org.fusesource.jansi.Ansi;

import java.io.PrintStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.mcreater.amclcore.MetaData.isUseAnsiOutputOverride;
import static org.fusesource.jansi.Ansi.ansi;

public class OutputParser {
    @Getter
    @Setter
    private static ColorTheme colorTheme = new DefaultTheme();
    private static final Pattern MC_DEFAULT_FORMAT = Pattern.compile("\\[(?<time>.*)] \\[(?<thread>.*)/(?<type>.*)]: (?<message>.*)");
    private static final Pattern JAVA_EXC_MAIN = Pattern.compile("Exception in thread \"(?<thread>.*)\" (?<excname>.*): (?<message>.*)");
    private static final Pattern JAVA_EXC_NAME = Pattern.compile("(?<excname>.*): (?<message>.*)");
    private static final Pattern JAVA_EXC_STACK = Pattern.compile("\tat (?<method>.*)((?<source>.*):(?<line>.*)) ~\\[(?<jar>.*):(?<moudle>.*)]");
    private static final Pattern JAVA_EXC_STACK2 = Pattern.compile("\tat (?<method>.*)((?<source>.*):(?<line>.*)) \\[(?<jar>.*):(?<moudle>.*)]");
    private static final Pattern JAVA_EXC_STACK_BASE = Pattern.compile("\tat (?<stack>.*)");
    private static final Pattern JAVA_EXC_END = Pattern.compile("\t\\.\\.\\. (?<stacks>.*) more");

    public enum OutputType {
        STDOUT,
        STDERR
    }

    public enum LogType {
        FATAL,
        ERROR,
        WARN,
        INFO,
        DEBUG,
        TRACE
    }

    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @Data
    public static class OutputLine {
        private final String data;
        private final OutputType type;

        public LogType getLogType() {
            Matcher matcher = parse();
            if (!matcher.find()) {
                if (
                        JAVA_EXC_MAIN.matcher(data).find() ||
                                JAVA_EXC_NAME.matcher(data).find() ||
                                JAVA_EXC_STACK.matcher(data).find() ||
                                JAVA_EXC_STACK2.matcher(data).find() ||
                                JAVA_EXC_STACK_BASE.matcher(data).find() ||
                                JAVA_EXC_END.matcher(data).find()) {
                    return LogType.ERROR;
                } else {
                    return type == OutputType.STDERR ? LogType.WARN : LogType.INFO;
                }
            }
            String logType = matcher.group("type");
            switch (type) {
                default:
                case STDOUT:
                    switch (logType.toLowerCase()) {
                        case "fatal":
                            return LogType.FATAL;
                        case "error":
                            return LogType.ERROR;
                        case "warn":
                            return LogType.WARN;
                        default:
                        case "info":
                            return LogType.INFO;
                        case "debug":
                            return LogType.DEBUG;
                        case "trace":
                            return LogType.TRACE;
                    }
                case STDERR:
                    switch (logType.toLowerCase()) {
                        case "fatal":
                            return LogType.FATAL;
                        case "error":
                            return LogType.ERROR;
                        default:
                        case "warn":
                            return LogType.WARN;
                    }
            }
        }

        public void printAnsi() {
            System.out.print("\r");
            if (!isUseAnsiOutputOverride()) {
                System.out.println(data);
                return;
            }

            Ansi ansi = ansi();
            Ansi.Consumer c;
            switch (getLogType()) {
                case FATAL:
                    c = colorTheme.applyFatal();
                    break;
                case ERROR:
                    c = colorTheme.applyError();
                    break;
                case WARN:
                    c = colorTheme.applyWarning();
                    break;
                default:
                case INFO:
                    c = colorTheme.applyInfo();
                    break;
                case DEBUG:
                    c = colorTheme.applyDebug();
                    break;
                case TRACE:
                    c = colorTheme.applyTrace();
                    break;
            }

            System.out.println(ansi.apply(c).a(data).eraseLine());
        }

        private Matcher parse() {
            return MC_DEFAULT_FORMAT.matcher(data);
        }

        public static OutputLine create(String data, OutputType type) {
            return new OutputLine(data, type);
        }

        public static OutputLine create(String data, PrintStream stream) {
            return new OutputLine(data, stream == System.err ? OutputType.STDERR : OutputType.STDOUT);
        }
    }
}
