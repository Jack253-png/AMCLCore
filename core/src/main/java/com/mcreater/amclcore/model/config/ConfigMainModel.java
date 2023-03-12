package com.mcreater.amclcore.model.config;

import com.mcreater.amclcore.model.ConfigModel;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ConfigMainModel implements ConfigModel {
    private ConfigLaunchModel launchConfig;
}
