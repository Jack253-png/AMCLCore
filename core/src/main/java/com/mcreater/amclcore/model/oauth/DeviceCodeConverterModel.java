package com.mcreater.amclcore.model.oauth;

import com.mcreater.amclcore.annotations.OAuthLoginModel;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
@OAuthLoginModel
public class DeviceCodeConverterModel {
    private TokenResponseModel model;
    private boolean isDevice;

    public String createAccessToken() {
        return isDevice() ? "d=" + getModel().getAccessToken() : getModel().getAccessToken();
    }
}
