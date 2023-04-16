package com.mcreater.amclcore.model.oauth;

import com.mcreater.amclcore.model.signatures.MinecraftProductSignature;
import lombok.Data;

import java.util.List;

@Data
public class MinecraftProductRequestModel {
    private List<MinecraftProductItemModel> items;
    private MinecraftProductSignature signature;
    private String keyId;

    @Data
    public static class MinecraftProductItemModel {
        private String name;
        private MinecraftProductSignature signature;
    }
}
