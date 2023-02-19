package com.mcreater.amclcore.model.config;

import com.mcreater.amclcore.model.ConfigModel;
import lombok.Data;

@Data
public class ConfigMemoryModel implements ConfigModel {
    private long minMemory;
    private long maxMemory;
}
