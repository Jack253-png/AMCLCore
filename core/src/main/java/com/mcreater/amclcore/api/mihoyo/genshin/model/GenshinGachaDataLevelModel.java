package com.mcreater.amclcore.api.mihoyo.genshin.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GenshinGachaDataLevelModel {
    private int id;
    private String name;
    private String color;
    private String type;
}
