package com.mcreater.amclcore.model.oauth;

import com.google.gson.annotations.SerializedName;
import com.mcreater.amclcore.annotations.OAuthLoginModel;
import com.mcreater.amclcore.annotations.RequestModel;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@OAuthLoginModel
@RequestModel
public class MinecraftRequestModel {
    private UUID username;
    private List<Object> roles;
    private Map<String, Object> metadata;
    @SerializedName("access_token")
    private String accessToken;
    @SerializedName("expires_in")
    private int expiresIn;
    @SerializedName("token_type")
    private String tokenType;
}
