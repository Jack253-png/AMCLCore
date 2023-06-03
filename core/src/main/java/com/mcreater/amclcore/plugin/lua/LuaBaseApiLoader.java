package com.mcreater.amclcore.plugin.lua;

import com.mcreater.amclcore.plugin.lua.lib.I18NLib;
import com.mcreater.amclcore.resources.ResourceFetcher;
import org.luaj.vm2.Globals;
import org.luaj.vm2.LoadState;
import org.luaj.vm2.compiler.LuaC;
import org.luaj.vm2.lib.jse.JsePlatform;

import java.io.InputStreamReader;

public class LuaBaseApiLoader {
    private static final Globals globals = JsePlatform.standardGlobals();

    public static void init() {
        globals.load(new I18NLib());
        LoadState.install(globals);
        LuaC.install(globals);
    }

    public static void load() {
        globals.load(new InputStreamReader(ResourceFetcher.get("assets/amclcore/test.lua")), "test.lua").call();
    }
}
