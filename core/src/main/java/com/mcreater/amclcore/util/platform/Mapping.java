package com.mcreater.amclcore.util.platform;

import com.google.gson.JsonElement;
import com.mcreater.amclcore.game.GameManifestJson;
import com.mcreater.amclcore.model.game.GameLibNativeReplacementModel;
import com.mcreater.amclcore.model.game.lib.GameDependedLibModel;
import com.mcreater.amclcore.resources.ResourceFetcher;
import com.mcreater.amclcore.util.maven.MavenLibName;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import static com.mcreater.amclcore.util.JsonUtil.GSON_PARSER;

public class Mapping {
    private static GameLibNativeReplacementModel replacementModel = GSON_PARSER.fromJson(
            new InputStreamReader(ResourceFetcher.get("amclcore", "natives/natives.json")),
            GameLibNativeReplacementModel.class
    );

    public static GameLibNativeReplacementModel getReplacementModel() {
        return replacementModel;
    }

    public static String getNativeName() {
        switch (OperatingSystem.CURRENT_OS) {
            case WINDOWS:
                switch (Architecture.SYSTEM_ARCH) {
                    case X86_64:
                        return "natives-windows";
                    case X86:
                        return "natives-windows-x86";
                    case ARM64:
                        return "natives-windows-arm64";
                }
            case OSX:
                switch (Architecture.SYSTEM_ARCH) {
                    case X86_64:
                        return "natives-macos";
                    case ARM64:
                        return "natives-macos-arm64";
                }
            case LINUX:
                switch (Architecture.SYSTEM_ARCH) {
                    case X86_64:
                        return "natives-linux";
                    case ARM32:
                        return "natives-linux-arm32";
                    case ARM64:
                        return "natives-linux-arm64";
                    case MIPS64EL:
                        return "natives-linux-mips64el";
                    case LOONGARCH64:
                        return "natives-linux-loongarch64";
                    case LOONGARCH64_OW:
                        return "natives-linux-loongarch64_ow";
                }
        }
        return null;
    }

    public static void patchVersion(GameManifestJson json) throws IOException {
        JsonElement element = GSON_PARSER.toJsonTree(
                GSON_PARSER.fromJson(
                        new InputStreamReader(Files.newInputStream(json.getJsonPath())),
                        Map.class
                )
        );

        if (element.isJsonObject()) {
            List<JsonElement> removed = new Vector<>();
            List<JsonElement> replacements = new Vector<>();
            HashMap<MavenLibName, GameDependedLibModel> mp = replacementModel.get(getNativeName().replace("natives-", ""));
            if (mp != null) {
                element.getAsJsonObject().getAsJsonArray("libraries").forEach(e -> {
                    GameDependedLibModel conv = GSON_PARSER.fromJson(e, GameDependedLibModel.class);
                    if (conv.hasNatives() || conv.getName().getGroupId().equals("org.lwjgl")) {
                        if (!conv.isNormalLib() || conv.getNatives() == null) {
                            mp.forEach((n, gameDependedLibModel) -> {
                                if (conv.getName().getGroupId().equals(n.getName()) && conv.getName().getArtifactId().equals(n.getArtifactId()) && conv.getName().getVersion().equals(n.getVersion()) &&
                                        n.getPlatform().contains(conv.getName().getPlatform())) {
                                    if (gameDependedLibModel != null)
                                        replacements.add(GSON_PARSER.toJsonTree(gameDependedLibModel, GameDependedLibModel.class));
                                }
                            });
                        }
                    } else if (conv.getNatives() != null) {
                        removed.add(e);
                        mp.forEach((n, gameDependedLibModel) -> {
                            if (conv.getName().getGroupId().equals(n.getName()) && conv.getName().getArtifactId().equals(n.getArtifactId()) && conv.getName().getVersion().equals(n.getVersion())) {
                                if (gameDependedLibModel != null)
                                    replacements.add(GSON_PARSER.toJsonTree(gameDependedLibModel, GameDependedLibModel.class));
                            }
                        });
                    }
                });

                removed.forEach(jsonElement -> element.getAsJsonObject().getAsJsonArray("libraries").remove(jsonElement));
                replacements.forEach(jsonElement -> element.getAsJsonObject().getAsJsonArray("libraries").add(jsonElement));
            }
        }
        GSON_PARSER.toJson(
                element,
                Files.newBufferedWriter(json.getJsonPath(), StandardOpenOption.WRITE)
        );
    }
}
