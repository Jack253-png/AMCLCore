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
public class DeviceCodeModel {
    @SerializedName("user_code")
    private String userCode;
    @SerializedName("device_code")
    private String deviceCode;
    @SerializedName("verification_uri")
    private String verificationUri;
    @SerializedName("expires_in")
    private int expiresIn;
    private int interval;
}