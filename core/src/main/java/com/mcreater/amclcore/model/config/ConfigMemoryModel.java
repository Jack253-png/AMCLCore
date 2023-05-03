package com.mcreater.amclcore.model.config;

import com.mcreater.amclcore.annotations.ConfigModel;
import com.mcreater.amclcore.java.MemorySize;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@ConfigModel
public class ConfigMemoryModel {
    private MemorySize minMemory;
    private MemorySize maxMemory;
}
