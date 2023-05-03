package com.mcreater.amclcore.java;

import com.mcreater.amclcore.command.CommandArg;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class JVMArgument {
    private String raw;

    public static JVMArgument create(String raw) {
        return new JVMArgument(raw);
    }

    public CommandArg toCommandArg() {
        return toCommandArg(new HashMap<>());
    }

    public CommandArg toCommandArg(@NotNull Map<String, Object> parseMap) {
        AtomicReference<String> result = new AtomicReference<>(raw);
        parseMap.forEach((s, o) -> {
            String rep = String.format("${%s}", s);
            if (raw.contains(rep)) result.set(raw.replace(rep, o.toString()));
        });
        return CommandArg.create(result.get());
    }
}
