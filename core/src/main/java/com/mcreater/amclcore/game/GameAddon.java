package com.mcreater.amclcore.game;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GameAddon {
    public enum Type {
        FORGE,
        LITELOADER,
        OPTIFINE,
        FABRIC,
        QUILT,
    }

    private final Type addonType;
    private final String version;
}
