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
    private static final Pattern JAVA_EXC_NAME = Pattern.compile("(?<excname>.*): (?<message>.*)");
    private static final Pattern JAVA_EXC_STACK = Pattern.compile("\tat (?<method>.*)((?<source>.*):(?<line>.*)) ~\\[(?<jar>.*):(?<moudle>.*)]");
    private static final Pattern JAVA_EXC_STACK2 = Pattern.compile("\tat (?<method>.*)((?<source>.*):(?<line>.*)) \\[(?<jar>.*):(?<moudle>.*)]");
    private static final Pattern JAVA_EXC_STACK_BASE = Pattern.compile("\tat (?<stack>.*)");
    private static final Pattern JAVA_EXC_END = Pattern.compile("\t\\.\\.\\. (?<stacks>.*) more");

    public enum OutputType {
        STDOUT,
        STDERR
    }

    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @Data
    public static class OutputLine {
        private final String data;
        private final OutputType type;

        public void printAnsi() {
            System.out.print("\r");
            if (!isUseAnsiOutputOverride()) {
                System.out.println(data);
                return;
            }
            Matcher matcher = parse();
            if (!matcher.find()) {
                if (
                        JAVA_EXC_NAME.matcher(data).find() ||
                                JAVA_EXC_STACK.matcher(data).find() ||
                                JAVA_EXC_STACK2.matcher(data).find() ||
                                JAVA_EXC_STACK_BASE.matcher(data).find() ||
                                JAVA_EXC_END.matcher(data).find()) {
                    System.out.println(ansi()
                            .apply(colorTheme.applyError())
                            .a(data)
                            .reset());
                } else {
                    System.out.println(ansi()
                            .apply(type == OutputType.STDERR ? colorTheme.applyWarning() : colorTheme.applyInfo())
                            .a(data)
                            .reset()
                    );
                }
                return;
            }
            String logType = matcher.group("type");
            Ansi ansi = ansi();
            Ansi.Consumer c;
            switch (type) {
                default:
                case STDOUT:
                    switch (logType.toLowerCase()) {
                        case "fatal":
                            c = colorTheme.applyFatal();
                            break;
                        case "error":
                            c = colorTheme.applyError();
                            break;
                        case "warn":
                            c = colorTheme.applyWarning();
                            break;
                        default:
                        case "info":
                            c = colorTheme.applyInfo();
                            break;
                        case "debug":
                            c = colorTheme.applyDebug();
                            break;
                        case "trace":
                            c = colorTheme.applyTrace();
                            break;
                    }
                    break;
                case STDERR:
                    switch (logType.toLowerCase()) {
                        case "fatal":
                            c = colorTheme.applyFatal();
                            break;
                        case "error":
                            c = colorTheme.applyError();
                            break;
                        default:
                        case "warn":
                            c = colorTheme.applyWarning();
                            break;
                    }
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
