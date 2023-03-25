package com.mcreater.amclcore.model.config;

import com.mcreater.amclcore.annotations.ConfigModel;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@ConfigModel
public class ConfigMainModel {
    private ConfigLaunchModel launchConfig;
}
