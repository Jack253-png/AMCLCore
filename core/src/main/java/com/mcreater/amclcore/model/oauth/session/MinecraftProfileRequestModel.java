package com.mcreater.amclcore.model.oauth.session;

import com.mcreater.amclcore.annotations.RequestModel;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Builder
@Data
@RequestModel
public class MinecraftProfileRequestModel {
    private UUID id;
    private String name;
    private List<MinecraftProfileSkinModel> skins;
    private List<MinecraftProfileCapeModel> capes;
    private Map<Object, Object> profileActions;

    @Data
    @Builder
    @RequestModel
    public static class MinecraftProfileSkinModel {
        private UUID id;
        private String state;
        private String url;
        private String variant;
    }

    @Data
    @Builder
    @RequestModel
    public static class MinecraftProfileCapeModel {
        private UUID id;
        private String state;
        private String url;
        private String alias;
    }
}
