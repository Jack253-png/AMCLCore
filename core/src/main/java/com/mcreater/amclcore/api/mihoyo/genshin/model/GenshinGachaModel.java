package com.mcreater.amclcore.api.mihoyo.genshin.model;

import com.mcreater.amclcore.annotations.RequestModel;
import lombok.Data;

@Data
@RequestModel
public class GenshinGachaModel {
    private int retcore;
    private String message;
    private GenshinGachaDataModel data;
}
