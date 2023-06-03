package com.mcreater.amclcore.model.oauth;

import com.mcreater.amclcore.annotations.OAuthLoginModel;
import com.mcreater.amclcore.annotations.RequestModel;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@OAuthLoginModel
@RequestModel
public class MinecraftProductRequestModel {
    private List<MinecraftProductItemModel> items;
    private String signature;
    private String keyId;

    @Data
    @Builder
    @OAuthLoginModel
    @RequestModel
    public static class MinecraftProductItemModel {
        private String name;
        private String signature;
    }
}
