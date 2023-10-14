package com.mcreater.amclcore.command.script

import com.mcreater.amclcore.command.CommandArg
import java.util.stream.Collectors

class PowerShellScriptProvider private constructor() : ScriptProvider {
    override fun toScript(args: List<CommandArg?>?): String? {
        return "& " + args?.stream()
            ?.map { it.toString() }
            ?.map { "\"" + it + "\"" }
            ?.collect(Collectors.joining(" "))
    }

    override val fileExtension: String
        get() = ".ps1"

    companion object {
        val INSTANCE = PowerShellScriptProvider()
    }
}
