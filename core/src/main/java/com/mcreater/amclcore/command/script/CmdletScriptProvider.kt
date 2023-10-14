package com.mcreater.amclcore.command.script

import com.mcreater.amclcore.command.CommandArg
import java.util.stream.Collectors


class CmdletScriptProvider private constructor() : ScriptProvider {
    override fun toScript(args: List<CommandArg?>?): String? {
        return args?.stream()
            ?.map { it.toString() }
            ?.map { if (it.contains(" ")) "\"" + it + "\"" else it }
            ?.collect(Collectors.joining(" "))
    }

    override val fileExtension: String
        get() = ".bat"

    companion object {
        val INSTANCE = CmdletScriptProvider()
    }
}

