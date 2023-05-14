package com.mcreater.amclcore.model.config;

import com.mcreater.amclcore.annotations.ConfigModel;
import com.mcreater.amclcore.java.JavaEnvironment;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@ConfigModel
public class ConfigLaunchModel {
    private ConfigMemoryModel memory;
    private List<JavaEnvironment> environments;
    private int selectedEnvironment;

    private String launcherNameOverride;
    private String launcherVersionOverride;

    public JavaEnvironment getEnv() {
        if (selectedEnvironment < environments.size()) return environments.get(selectedEnvironment);
        if (environments.size() > 0) return environments.get(0);
        return null;
    }
}
