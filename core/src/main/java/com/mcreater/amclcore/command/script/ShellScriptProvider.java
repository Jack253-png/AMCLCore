package com.mcreater.amclcore.command.script;

import com.mcreater.amclcore.command.CommandArg;

import java.util.List;
import java.util.stream.Collectors;

public class ShellScriptProvider implements ScriptProvider {
    public static final ShellScriptProvider INSTANCE = new ShellScriptProvider();

    private ShellScriptProvider() {
    }

    public String toScript(List<CommandArg> args) {
        return args.stream()
                .map(CommandArg::toString)
                .map(s -> "\"" + s + "\"")
                .collect(Collectors.joining(" "));
    }

    public String getFileExtension() {
        return ".sh";
    }
}
