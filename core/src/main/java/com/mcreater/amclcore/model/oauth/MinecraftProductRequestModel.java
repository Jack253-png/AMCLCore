package com.mcreater.amclcore.model.oauth;

import lombok.Data;

import java.util.List;

@Data
public class MinecraftProductRequestModel {
    private List<MinecraftProductItemModel> items;
    private String signature;
    private String keyId;

    @Data
    public static class MinecraftProductItemModel {
        private String name;
        private String signature;
    }
}
