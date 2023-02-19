package com.mcreater.amclcore.model.config;

import com.mcreater.amclcore.model.ConfigModel;
import lombok.Data;

@Data
public class ConfigMainModel implements ConfigModel {
    private ConfigMemoryModel memory;
}
