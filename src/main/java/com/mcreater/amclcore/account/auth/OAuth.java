package com.mcreater.amclcore.account.auth;

import com.mcreater.amclcore.concurrent.AbstractTask;
import com.mcreater.amclcore.concurrent.ConcurrentExecutors;
import com.mcreater.amclcore.concurrent.ConcurrentUtil;
import com.mcreater.amclcore.exceptions.OAuthTimeOutException;
import com.mcreater.amclcore.model.oauth.DeviceCodeConverterModel;
import com.mcreater.amclcore.model.oauth.DeviceCodeModel;
import com.mcreater.amclcore.model.oauth.TokenResponseModel;
import com.mcreater.amclcore.util.HttpClientWrapper;
import com.mcreater.amclcore.util.SwingUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
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

    @Getter
    private static final Consumer<DeviceCodeModel> defaultDevHandler = model2 -> ConcurrentExecutors.runAllTask(
            ConcurrentExecutors.AWT_EVENT_EXECUTOR,
            SwingUtil.copyContentAsync(model2.getUserCode()),
            SwingUtil.openBrowserAsync(model2.getVerificationUri())
    );

    protected DeviceCodeModel fetchDeviceToken(Consumer<DeviceCodeModel> requestHandler) throws URISyntaxException, IOException {
        DeviceCodeModel model = HttpClientWrapper.createNew(HttpClientWrapper.Method.GET)
                .requestURI(deviceCodeUrl)
                .requestURIParam("client_id", createClientID())
                .requestURIParam("scope", buildScopeString("XboxLive.signin", "offline_access"))
                .connectTimeout(5000)
                .connectionRequestTimeout(5000)
                .sendRequestAndReadJson(DeviceCodeModel.class);

        Optional.of(requestHandler).ifPresent(deviceCodeModelConsumer -> deviceCodeModelConsumer.accept(model));
        return model;
    }

    public TokenResponseModel checkToken(String deviceCode) throws URISyntaxException, IOException {
        return HttpClientWrapper.createNew(HttpClientWrapper.Method.POST)
                .requestURI(tokenUrl)
                .requestEntityEncodedURL(
                        new BasicNameValuePair("grant_type", buildScopeString2("urn", "ietf", "params", "oauth", "grant-type", "device_code")),
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

    private static String buildScopeString2(String... s) {
        return String.join(":", Arrays.asList(s));
    }

    private static String createClientID() {
        return readProperty(CLIENT_ID_PROPERTY_NAME, clientID);
    }

    @AllArgsConstructor
    public class OAuthLoginTask extends AbstractTask<DeviceCodeConverterModel> {
        private final Consumer<DeviceCodeModel> requestHandler;
        public DeviceCodeConverterModel call() throws Exception {
            DeviceCodeModel model = fetchDeviceToken(requestHandler);

            long startTime = System.nanoTime();
            int interval = model.getInterval();

            while (true) {
                ConcurrentUtil.sleepTime(Math.max(interval, 1));

                long estimatedTime = System.nanoTime() - startTime;
                if (TimeUnit.SECONDS.convert(estimatedTime, TimeUnit.NANOSECONDS) >= Math.min(model.getExpiresIn(), 900)) {
                    throw new OAuthTimeOutException();
                }

                TokenResponseModel checkIn = checkToken(model.getDeviceCode());

                if (checkIn.getError() == null) return DeviceCodeConverterModel
                                                          .builder()
                                                          .isDevice(true)
                                                          .model(checkIn)
                                                          .build();

                switch (checkIn.getError()) {
                    case "authorization_pending":
                        continue;
                    case "slow_down":
                        interval += 5;
                        continue;
                    case "expired_token":
                    case "invalid_grant":
                    default:
                        throw new OAuthTimeOutException();
                }
            }
        }
    }
}
