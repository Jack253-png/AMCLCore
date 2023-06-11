package com.mcreater.amclcore.model.config;

import com.mcreater.amclcore.annotations.ConfigModel;
import com.mcreater.amclcore.java.JavaEnvironment;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Optional;

@Data
@Builder
@ConfigModel
public class ConfigLaunchModel {
    private ConfigMemoryModel memory;
    private List<JavaEnvironment> environments;
    private int selectedEnvironment;

    private String launcherNameOverride;
    private String launcherVersionOverride;
    private boolean useSelfGamePath;
    private ConfigWindowSizeModel windowSize;

    public Optional<JavaEnvironment> getEnv() {
        if (selectedEnvironment < environments.size())
            return Optional.ofNullable(environments.get(selectedEnvironment));
        if (environments.size() > 0) return Optional.ofNullable(environments.get(0));
        return Optional.empty();
    }
}
