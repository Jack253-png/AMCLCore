package com.mcreater.amclcore.model.oauth;

import com.google.gson.annotations.SerializedName;
import com.mcreater.amclcore.annotations.OAuthLoginModel;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
@OAuthLoginModel
public class AuthCodeModel {
    @SerializedName("access_token")
    private String accessToken;
    @SerializedName("refresh_token")
    private String refreshToken;
    @SerializedName("expires_in")
    private int expiresIn;
}
