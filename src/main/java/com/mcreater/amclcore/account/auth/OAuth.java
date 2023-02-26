package com.mcreater.amclcore.account.auth;

import com.mcreater.amclcore.concurrent.AbstractTask;
import com.mcreater.amclcore.model.oauth.DeviceCodeModel;
import com.mcreater.amclcore.model.oauth.TokenResponseModel;
import com.mcreater.amclcore.util.HttpClientWrapper;
import lombok.AllArgsConstructor;
import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

import static com.mcreater.amclcore.util.PropertyUtil.readProperty;

@AllArgsConstructor
public class OAuth {
    public static final OAuth MICROSOFT = new OAuth(
            "login.microsoftonline.com/consumers/oauth2/v2.0/devicecode",
            "login.microsoftonline.com/consumers/oauth2/v2.0/token"
    );
    private final String deviceCodeUrl;
    private final String tokenUrl;
    private static final String clientID = "1a969022-f24f-4492-a91c-6f4a6fcb373c";
    public static final String CLIENT_ID_PROPERTY_NAME = "amclcore.oauth.clientid.override";

    protected DeviceCodeModel fetchDeviceToken(Consumer<DeviceCodeModel> requestHandler) throws URISyntaxException, IOException {
        DeviceCodeModel model = HttpClientWrapper.createNew(HttpClientWrapper.Method.GET)
                .requestURI(deviceCodeUrl)
                .requestURIParam("client_id", createClientID())
                .requestURIParam("scope", buildScopeString("XboxLive.signin", "openid", "profile", "offline_access"))
                .connectTimeout(5000)
                .connectionRequestTimeout(5000)
                .sendRequestAndReadJson(DeviceCodeModel.class);

        Optional.of(requestHandler).ifPresent(deviceCodeModelConsumer -> deviceCodeModelConsumer.accept(model));
        return model;
    }

    public TokenResponseModel checkToken(String deviceCode) throws URISyntaxException, IOException {
        Map<String, Object> data = new HashMap<>();
        data.put("grant_type", "urn:ietf:params:oauth:grant-type:device_code");
        data.put("client_id", createClientID());
        data.put("code", deviceCode);

        return HttpClientWrapper.createNew(HttpClientWrapper.Method.POST)
                .requestURI(tokenUrl)
                .requestHeader("Content-Type", "application/x-www-form-urlencoded")
                .requestEntityEncodedURL(
                        new BasicNameValuePair("grant_type", "urn:ietf:params:oauth:grant-type:device_code"),
                        new BasicNameValuePair("client_id", createClientID()),
                        new BasicNameValuePair("code", deviceCode)
                )
                .connectTimeout(5000)
                .connectionRequestTimeout(5000)
                .sendRequestAndReadJson(TokenResponseModel.class);
    }

    public OAuthLoginTask fetchDeviceTokenAsync(Consumer<DeviceCodeModel> requestHandler) {
        return new OAuthLoginTask(requestHandler);
    }

    private static String buildScopeString(String... s) {
        return String.join(" ", Arrays.asList(s));
    }

    private static String createClientID() {
        return readProperty(CLIENT_ID_PROPERTY_NAME, clientID);
    }

    @AllArgsConstructor
    public class OAuthLoginTask extends AbstractTask<DeviceCodeModel> {
        private final Consumer<DeviceCodeModel> requestHandler;
        public DeviceCodeModel call() throws Exception {
            return fetchDeviceToken(requestHandler);
        }
    }
}
