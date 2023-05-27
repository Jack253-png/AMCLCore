package com.mcreater.amclcore.command.ansi;

import java.awt.*;

public class DefaultTheme implements ColorTheme {
    public Color getFatal() {
        return new Color(234, 67, 54);
    }

    public Color getError() {
        return new Color(237, 80, 68);
    }

    public Color getWarning() {
        return new Color(164, 136, 21);
    }

    public Color getInfo() {
        return new Color(86, 148, 42);
    }

    public Color getDebug() {
        return new Color(83, 148, 236);
    }
}
