package com.mcreater.amclcore.command.script;

import com.mcreater.amclcore.command.CommandArg;

import java.util.List;
import java.util.stream.Collectors;

public class PowerShellScriptProvider implements ScriptProvider {
    public static final PowerShellScriptProvider INSTANCE = new PowerShellScriptProvider();

    private PowerShellScriptProvider() {
    }

    public String toScript(List<CommandArg> args) {
        return "& " + args.stream()
                .map(CommandArg::toString)
                .map(s -> "\"" + s + "\"")
                .collect(Collectors.joining(" "));
    }

    public String getFileExtension() {
        return ".ps1";
    }
}
