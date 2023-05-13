package com.mcreater.amclcore.model.game.rule;

import com.mcreater.amclcore.util.platform.Architecture;
import com.mcreater.amclcore.util.platform.OperatingSystem;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GameRuleModel {
    private Action action;
    private GameRuleOsModel os;
    private GameRuleFeatureModel features;

    // TODO to be implemented
    public boolean valid() {
        boolean osValid = os.getName() == null || OperatingSystem.CURRENT_OS.getCheckedName().equals(os.getName());
        boolean archValid = os.getArch() == null || Architecture.CURRENT_ARCH.getDisplayName().equals(os.getArch());

        return osValid && archValid && os.getVersion() == null && this.action == Action.ALLOW;
    }

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
