package com.mcreater.amclcore.command;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.PrintStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.fusesource.jansi.Ansi.ansi;

public class OutputParser {
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
            System.out.println(ansi().fgRgb(50, 50, 50).a("test").reset());
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
