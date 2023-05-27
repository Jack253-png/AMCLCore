package com.mcreater.amclcore.command.ansi;

import org.fusesource.jansi.Ansi;

import java.awt.*;

public interface ColorTheme {
    Color getFatal();

    Color getError();

    Color getWarning();

    Color getInfo();

    Color getDebug();

    default Ansi.Consumer apply(Color color) {
        return c -> {
            c.fgRgb(color.getRed(), color.getGreen(), color.getBlue());
        };
    }
}
