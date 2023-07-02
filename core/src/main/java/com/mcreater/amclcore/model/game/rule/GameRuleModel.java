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

    public boolean valid(GameRuleFeatureModel data) {
        if (features != null && data != null) {
            Boolean featuresDemoUserValid = null;
            Boolean featuresHasRes = null;

            if (features.getIsDemoUser() != null && data.getIsDemoUser() != null) {
                featuresDemoUserValid = features.getIsDemoUser() == data.getIsDemoUser();
            }

            if (features.getHasCustomResolution() != null && data.getHasCustomResolution() != null) {
                featuresHasRes = features.getHasCustomResolution() == data.getHasCustomResolution();
            }

            boolean dem = featuresDemoUserValid != null && featuresDemoUserValid;
            boolean hasRes = featuresHasRes != null && featuresHasRes;

            return dem || hasRes;
        }

        if (os == null) return true;
        boolean osValid = os.getName() == null || OperatingSystem.CURRENT_OS.getCheckedName().equals(os.getName());
        boolean archValid = os.getArch() == null || Architecture.CURRENT_ARCH.getDisplayName().equals(os.getArch());

        return osValid && archValid && os.getVersion() == null;
    }

    public boolean valid() {
        return valid(null);
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
