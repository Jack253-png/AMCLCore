package com.mcreater.amclcore.model.oauth;

import com.google.gson.annotations.SerializedName;
import com.mcreater.amclcore.annotations.OAuthLoginModel;
import com.mcreater.amclcore.annotations.RequestModel;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@OAuthLoginModel
@RequestModel
public class TokenResponseModel {
    @SerializedName("token_type")
    private String tokenType;
    @SerializedName("expires_in")
    private int expiresIn;
    @SerializedName("ext_expires_in")
    private int extExpiresIn;
    private String scope;
    @SerializedName("access_token")
    private String accessToken;
    @SerializedName("refresh_token")
    private String refreshToken;
    private String error;
    @SerializedName("error_description")
    private String errorDescription;
    @SerializedName("correlation_id")
    private String correlationId;
}