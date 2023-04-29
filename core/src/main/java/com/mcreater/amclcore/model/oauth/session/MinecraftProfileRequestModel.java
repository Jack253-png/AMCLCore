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
    // if exception
    private String path;
    private String errorType;
    private String error;
    private MinecraftNameChangeableRequestModel details;
    private String errorMessage;
    private String developerMessage;

    @Data
    @Builder
    @RequestModel
    public static class MinecraftProfileSkinModel {
        private UUID id;
        private State state;
        private String url;
        private Variant variant;
    }

    @Data
    @Builder
    @RequestModel
    public static class MinecraftProfileCapeModel {
        private UUID id;
        private State state;
        private String url;
        private String alias;

        public String createId() {
            return id.toString();
        }
    }

    public enum State {
        ACTIVE,
        INACTIVE;

        public static State parse(String s) {
            try {
                return valueOf(s);
            } catch (Exception e) {
                return INACTIVE;
            }
        }
    }

    public enum Variant {
        CLASSIC,
        SLIM;

        public static Variant parse(String s) {
            try {
                return valueOf(s);
            } catch (Exception e) {
                return CLASSIC;
            }
        }
    }
}
