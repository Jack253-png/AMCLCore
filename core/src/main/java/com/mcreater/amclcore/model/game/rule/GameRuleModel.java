package com.mcreater.amclcore.model.game.rule;

import com.mcreater.amclcore.util.platform.Architecture;
import com.mcreater.amclcore.util.platform.OperatingSystem;
import lombok.Builder;
import lombok.Data;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

@Data
@Builder
public class GameRuleModel {
    private Action action;
    private GameRuleOsModel os;
    private GameRuleFeatureModel features;

    // TODO to be implemented
    public boolean valid(GameRuleFeatureModel data) {
        if (os == null) return true;
        boolean osValid = os.getName() == null || OperatingSystem.CURRENT_OS.getCheckedName().equals(os.getName());
        boolean archValid = os.getArch() == null || Architecture.CURRENT_ARCH.getDisplayName().equals(os.getArch());

        AtomicBoolean featuresValid = new AtomicBoolean(true);
        Optional.ofNullable(data).ifPresent(m -> {
            if (data.getHasCustomResolution() != null && features.getHasCustomResolution() != null)
                featuresValid.set(featuresValid.get() && data.getHasCustomResolution() == features.getHasCustomResolution());
            if (data.getIsDemoUser() != null && features.getIsDemoUser() != null)
                featuresValid.set(featuresValid.get() && data.getIsDemoUser() == features.getIsDemoUser());
        });

        return osValid && archValid && os.getVersion() == null && featuresValid.get();
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
