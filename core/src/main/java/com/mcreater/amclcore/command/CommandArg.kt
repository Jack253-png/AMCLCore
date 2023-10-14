package com.mcreater.amclcore.command

import java.util.*


open class CommandArg protected constructor(private var command: String? = null) {
    fun parseMap(base: Map<String?, Any?>): CommandArg {
        base.forEach { (s: String?, o: Any?) ->
            val rep = String.format("\${%s}", s)
            if (command!!.contains(rep)) command = command!!.replace(rep, Objects.toString(o))
        }
        return this
    }

    override fun toString(): String {
        return command!!
    }

    companion object {
        @JvmStatic
        fun create(command: String?): CommandArg {
            return CommandArg(command)
        }
    }
}

