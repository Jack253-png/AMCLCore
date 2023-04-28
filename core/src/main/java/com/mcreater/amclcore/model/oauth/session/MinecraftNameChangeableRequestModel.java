package com.mcreater.amclcore.model.oauth.session;

import com.mcreater.amclcore.annotations.RequestModel;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@RequestModel
public class MinecraftNameChangeableRequestModel {
    private State status;

    public enum State {
        DUPLICATE,
        AVAILABLE,
        NOT_ALLOWED;

        public static State parse(String s) {
            try {
                return valueOf(s);
            } catch (Exception e) {
                return DUPLICATE;
            }
        }
    }
}
