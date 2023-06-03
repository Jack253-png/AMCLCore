package com.mcreater.amclcore.model.game.logging;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class GameLoggingModel {
    private GameClientLoggingModel client;
}
