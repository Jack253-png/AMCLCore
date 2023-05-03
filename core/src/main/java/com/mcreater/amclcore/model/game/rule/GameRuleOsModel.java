package com.mcreater.amclcore.model.game.rule;

import lombok.Builder;
import lombok.Data;

import java.util.regex.Pattern;

@Data
@Builder
public class GameRuleOsModel {
    private String name;
    @Builder.Default
    private Pattern version = Pattern.compile("");
    @Builder.Default
    private String arch = "";
}
