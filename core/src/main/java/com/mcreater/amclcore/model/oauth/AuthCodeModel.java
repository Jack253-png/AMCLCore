package com.mcreater.amclcore.model.oauth;

import com.google.gson.annotations.SerializedName;
import com.mcreater.amclcore.annotations.OAuthLoginModel;
import com.mcreater.amclcore.annotations.RequestModel;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
@OAuthLoginModel
@RequestModel
public class AuthCodeModel {
    @SerializedName("access_token")
    private String accessToken;
    @SerializedName("refresh_token")
    private String refreshToken;
    @SerializedName("expires_in")
    private int expiresIn;
}
