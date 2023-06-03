package com.mcreater.amclcore.command;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Objects;

@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class CommandArg {
    private String command;

    public static CommandArg create(String command) {
        return new CommandArg(command);
    }

    public CommandArg parseMap(@NotNull Map<String, Object> base) {
        base.forEach((s, o) -> {
            String rep = String.format("${%s}", s);
            if (command.contains(rep)) command = command.replace(rep, Objects.toString(o));
        });
        return this;
    }

    public String toString() {
        return command;
    }
}
