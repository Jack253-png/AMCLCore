package com.mcreater.amclcore.plugin.lua

import com.mcreater.amclcore.plugin.lua.lib.I18NLib
import com.mcreater.amclcore.resources.ResourceFetcher
import org.luaj.vm2.LoadState
import org.luaj.vm2.compiler.LuaC
import org.luaj.vm2.lib.jse.JsePlatform
import java.io.InputStreamReader


class LuaBaseApiLoader {
    companion object {
        @JvmStatic
        private val globals = JsePlatform.standardGlobals()

        @JvmStatic
        fun init() {
            globals.load(I18NLib())
            LoadState.install(globals)
            LuaC.install(globals)
        }

        @JvmStatic
        fun load() {
            globals.load(InputStreamReader(ResourceFetcher.get("amclcore", "test.lua")), "test.lua").call()
        }
    }
}

