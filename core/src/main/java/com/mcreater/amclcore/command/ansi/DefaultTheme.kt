package com.mcreater.amclcore.command.ansi

import org.fusesource.jansi.Ansi


class DefaultTheme : ColorTheme {
    override fun applyFatal(): Ansi.Consumer {
        return Ansi.Consumer { it.fg(Ansi.Color.RED) }
    }

    override fun applyError(): Ansi.Consumer {
        return Ansi.Consumer { it.fg(Ansi.Color.RED) }
    }

    override fun applyWarning(): Ansi.Consumer {
        return Ansi.Consumer { it.fg(Ansi.Color.YELLOW) }
    }

    override fun applyInfo(): Ansi.Consumer {
        return Ansi.Consumer { it.fg(Ansi.Color.GREEN) }
    }

    override fun applyDebug(): Ansi.Consumer {
        return Ansi.Consumer { it.fg(Ansi.Color.CYAN) }
    }

    override fun applyTrace(): Ansi.Consumer? {
        return Ansi.Consumer { it.fg(Ansi.Color.BLUE) }
    }
}

