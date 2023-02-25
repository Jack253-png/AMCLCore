package com.mcreater.amclcore.account.auth;

import com.google.gson.annotations.SerializedName;
import com.mcreater.amclcore.concurrent.oauth.LoginCodeTask;
import com.mcreater.amclcore.util.HttpClientWrapper;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.io.IOException;
import java.net.URISyntaxException;

@AllArgsConstructor
public class OAuth {
    public static final OAuth MICROSOFT = new OAuth(
            "login.microsoftonline.com/consumers/oauth2/v2.0/devicecode"
    );
    private final String tokenUrl;
    private static final String clientId = "1a969022-f24f-4492-a91c-6f4a6fcb373c";

    @Data
    @Builder
    public static class DeviceCodeModel {
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

    public DeviceCodeModel createDeviceToken() throws URISyntaxException, IOException {
        DeviceCodeModel model = HttpClientWrapper.createNew(HttpClientWrapper.Method.GET)
                .requestURI(tokenUrl)
                .requestURIParam("client_id", clientId)
                .requestURIParam("scope", "XboxLive.signin offline_access")
                .connectTimeout(5000)
                .connectionRequestTimeout(5000)
                .sendRequestAndReadJson(DeviceCodeModel.class);

        System.out.println(model.getUserCode());
        return model;
    }

    public LoginCodeTask createDeviceTokenAsync() {
        return new LoginCodeTask();
    }
}
