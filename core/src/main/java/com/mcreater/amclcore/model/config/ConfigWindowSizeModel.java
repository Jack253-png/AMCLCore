package com.mcreater.amclcore.model.config;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@AllArgsConstructor
@Data
@Builder
public class ConfigWindowSizeModel {
    private int width;
    private int height;
    private boolean isFullscreen;
}
