package com.mcreater.amclcore.model.game.lib;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class GameJavaComponentModel {
    private String component;
    private int majorVersion;
}
