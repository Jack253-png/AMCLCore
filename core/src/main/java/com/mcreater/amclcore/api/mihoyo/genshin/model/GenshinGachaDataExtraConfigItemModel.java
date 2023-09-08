package com.mcreater.amclcore.api.mihoyo.genshin.model;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class GenshinGachaDataExtraConfigItemModel {
    private int key;
    private List<GenshinGachaDataLevelModel> value;
}
