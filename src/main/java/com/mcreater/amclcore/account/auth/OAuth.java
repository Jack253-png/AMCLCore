package com.mcreater.amclcore.account.auth;

import com.mcreater.amclcore.concurrent.AbstractTask;
import com.mcreater.amclcore.concurrent.ConcurrentExecutors;
import com.mcreater.amclcore.model.oauth.DeviceCodeModel;
import com.mcreater.amclcore.util.HttpClientWrapper;
import com.mcreater.amclcore.util.SwingUtils;
import lombok.AllArgsConstructor;

import java.io.IOException;
import java.net.URISyntaxException;

@AllArgsConstructor
public class OAuth {
    public static final OAuth MICROSOFT = new OAuth(
            "login.microsoftonline.com/consumers/oauth2/v2.0/devicecode"
    );
    private final String tokenUrl;
    private static final String clientID = "1a969022-f24f-4492-a91c-6f4a6fcb373c";

    protected DeviceCodeModel createDeviceToken() throws URISyntaxException, IOException {
        DeviceCodeModel model = HttpClientWrapper.createNew(HttpClientWrapper.Method.GET)
                .requestURI(tokenUrl)
                .requestURIParam("client_id", clientID)
                .requestURIParam("scope", "XboxLive.signin offline_access")
                .connectTimeout(5000)
                .connectionRequestTimeout(5000)
                .sendRequestAndReadJson(DeviceCodeModel.class);

        System.out.println(model.getUserCode());
        return model;
    }

    public OAuthLoginTask createDeviceTokenAsync() {
        return new OAuthLoginTask();
    }

    public class OAuthLoginTask extends AbstractTask<DeviceCodeModel> {
        public DeviceCodeModel call() throws Exception {
            DeviceCodeModel model = createDeviceToken();
            ConcurrentExecutors.OAUTH_LOGIN_EXECUTOR.submit(SwingUtils.openBrowser(model.getVerificationUri())).get();
            return model;
        }
    }
}
