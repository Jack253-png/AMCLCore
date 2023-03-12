package com.mcreater.amclcore.model.oauth;

import com.google.gson.annotations.SerializedName;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
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