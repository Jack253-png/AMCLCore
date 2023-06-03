package com.mcreater.amclcore.model.game.rule;

import lombok.Builder;
import lombok.Data;

import java.util.regex.Pattern;

@Data
@Builder
public class GameRuleOsModel {
    private String name;
    private Pattern version;
    private String arch;
}
