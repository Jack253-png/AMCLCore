package com.mcreater.amclcore.model.game.rule;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GameRuleModel {
    private Action action;
    private GameRuleOsModel os;
    private GameRuleFeatureModel features;

    public enum Action {
        ALLOW,
        DISALLOW;

        public static Action parse(String s) {
            try {
                return valueOf(s.toUpperCase());
            } catch (Exception e) {
                return DISALLOW;
            }
        }
    }
}
