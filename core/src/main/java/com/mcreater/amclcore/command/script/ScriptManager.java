package com.mcreater.amclcore.command.script;

import com.mcreater.amclcore.command.CommandArg;

import java.util.*;

import static com.mcreater.amclcore.i18n.I18NManager.translatable;

public class ScriptManager {
    private static final Map<String, ScriptProvider> scriptProviders = new HashMap<>();

    static {
        register("powershell", PowerShellScriptProvider.Companion.getINSTANCE());
        register("cmdlet", CmdletScriptProvider.Companion.getINSTANCE());
        register("sh", ShellScriptProvider.Companion.getINSTANCE());
    }

    public static List<String> getProviders() {
        return new Vector<>(scriptProviders.keySet());
    }

    public static String getScriptDesc(String id) {
        return translatable(String.format("core.script.%s.desc", id)).getText();
    }

    public static String toScript(String id, List<CommandArg> args) {
        return Optional.ofNullable(scriptProviders.get(id)).map(sp -> sp.toScript(args)).orElse("");
    }

    public static void register(String id, ScriptProvider provider) {
        scriptProviders.put(id, provider);
    }
}
