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
            if (!isUseAnsiOutputOverride()) {
                System.out.println(data);
                return;
            }
            Matcher matcher = parse();
            if (!matcher.find()) {
                System.out.println(ansi()
                        .apply(colorTheme.apply(type == OutputType.STDERR ? colorTheme.getWarning() : colorTheme.getInfo()))
                        .a(data)
                        .reset()
                );
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
                            c = colorTheme.apply(colorTheme.getFatal());
                            break;
                        case "error":
                            c = colorTheme.apply(colorTheme.getError());
                            break;
                        case "warn":
                            c = colorTheme.apply(colorTheme.getWarning());
                            break;
                        default:
                        case "info":
                            c = colorTheme.apply(colorTheme.getInfo());
                            break;
                        case "debug":
                            c = colorTheme.apply(colorTheme.getDebug());
                            break;
                    }
                    break;
                case STDERR:
                    switch (logType.toLowerCase()) {
                        case "fatal":
                            c = colorTheme.apply(colorTheme.getFatal());
                            break;
                        case "error":
                            c = colorTheme.apply(colorTheme.getError());
                            break;
                        default:
                        case "warn":
                            c = colorTheme.apply(colorTheme.getWarning());
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
