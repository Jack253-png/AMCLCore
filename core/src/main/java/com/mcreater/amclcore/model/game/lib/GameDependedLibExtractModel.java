package com.mcreater.amclcore.model.game.lib;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class GameDependedLibExtractModel {
    private List<String> exclude;
}
