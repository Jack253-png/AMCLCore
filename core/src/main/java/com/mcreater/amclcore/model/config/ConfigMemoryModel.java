package com.mcreater.amclcore.model.config;

import com.mcreater.amclcore.model.ConfigModel;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ConfigMemoryModel implements ConfigModel {
    private long minMemory;
    private long maxMemory;
}
