package com.mcreater.amclcore.command.script;

import com.mcreater.amclcore.command.CommandArg;

import java.util.List;

public interface ScriptProvider {
    String toScript(List<CommandArg> args);

    String getFileExtension();
}
