package com.mcreater.amclcore.command.ansi;

import org.fusesource.jansi.Ansi;

public class DefaultTheme implements ColorTheme {
    public Ansi.Consumer applyFatal() {
        return a -> a.fg(Ansi.Color.RED);
    }

    public Ansi.Consumer applyError() {
        return a -> a.fg(Ansi.Color.RED);
    }

    public Ansi.Consumer applyWarning() {
        return a -> a.fg(Ansi.Color.YELLOW);
    }

    public Ansi.Consumer applyInfo() {
        return a -> a.fg(Ansi.Color.GREEN);
    }

    public Ansi.Consumer applyDebug() {
        return a -> a.fg(Ansi.Color.CYAN);
    }

    public Ansi.Consumer applyTrace() {
        return a -> a.fg(Ansi.Color.BLUE);
    }
}
