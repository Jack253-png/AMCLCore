package com.mcreater.amclcore.command.script

import com.mcreater.amclcore.command.CommandArg


interface ScriptProvider {
    fun toScript(args: List<CommandArg?>?): String?
    val fileExtension: String?
}

