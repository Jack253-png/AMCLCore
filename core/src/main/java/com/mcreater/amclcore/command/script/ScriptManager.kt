package com.mcreater.amclcore.command.script

import com.mcreater.amclcore.command.CommandArg
import com.mcreater.amclcore.i18n.I18NManager
import com.mcreater.amclcore.i18n.Text
import java.util.*


class ScriptManager {
    companion object {
        @JvmStatic
        private val scriptProviders: MutableMap<String, ScriptProvider> = HashMap()

        init {
            register("powershell", PowerShellScriptProvider.INSTANCE)
            register("cmdlet", CmdletScriptProvider.INSTANCE)
            register("sh", ShellScriptProvider.INSTANCE)
        }

        @JvmStatic
        val providers: List<String>
            get() = Vector(scriptProviders.keys)

        @JvmStatic
        fun getScriptDesc(id: String?): Text {
            return I18NManager.translatable(String.format("core.script.%s.desc", id))
        }

        @JvmStatic
        fun toScript(id: String, args: List<CommandArg?>?): String? {
            return Optional.ofNullable(scriptProviders[id]).map { sp: ScriptProvider ->
                sp.toScript(
                    args
                )
            }.orElse("")
        }

        @JvmStatic
        fun register(id: String, provider: ScriptProvider) {
            scriptProviders[id] = provider
        }
    }
}
