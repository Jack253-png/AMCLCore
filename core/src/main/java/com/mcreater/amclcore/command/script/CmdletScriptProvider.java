package com.mcreater.amclcore.command.script;

import com.mcreater.amclcore.command.CommandArg;

import java.util.List;
import java.util.stream.Collectors;

public class CmdletScriptProvider implements ScriptProvider {
    public static final CmdletScriptProvider INSTANCE = new CmdletScriptProvider();

    private CmdletScriptProvider() {
    }

    public String toScript(List<CommandArg> args) {
        return args.stream()
                .map(CommandArg::toString)
                .map(s -> s.contains(" ") ? ("\"" + s + "\"") : s)
                .collect(Collectors.joining(" "));
    }

    public String getFileExtension() {
        return ".bat";
    }
}
