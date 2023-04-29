package com.mcreater.amclcore.model.oauth.session;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MinecraftEnableCapeResponseModel {
    private String capeId;
}
