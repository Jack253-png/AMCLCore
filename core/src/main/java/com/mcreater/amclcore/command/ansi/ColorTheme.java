package com.mcreater.amclcore.command.ansi;

import org.fusesource.jansi.Ansi;

public interface ColorTheme {
    Ansi.Consumer applyFatal();

    Ansi.Consumer applyError();

    Ansi.Consumer applyWarning();

    Ansi.Consumer applyInfo();

    Ansi.Consumer applyDebug();

    Ansi.Consumer applyTrace();
}
