package com.mcreater.amclcore.command.ansi

import org.fusesource.jansi.Ansi


interface ColorTheme {
    fun applyFatal(): Ansi.Consumer?
    fun applyError(): Ansi.Consumer?
    fun applyWarning(): Ansi.Consumer?
    fun applyInfo(): Ansi.Consumer?
    fun applyDebug(): Ansi.Consumer?
    fun applyTrace(): Ansi.Consumer?
}
