package com.mcreater.amclcore.account.auth;

import com.mcreater.amclcore.concurrent.AbstractTask;
import com.mcreater.amclcore.concurrent.ConcurrentExecutors;
import com.mcreater.amclcore.concurrent.TaskState;
import com.mcreater.amclcore.concurrent.TaskStates;
import com.mcreater.amclcore.exceptions.oauth.OAuthTimeOutException;
import com.mcreater.amclcore.exceptions.oauth.OAuthUserHashException;
import com.mcreater.amclcore.exceptions.oauth.OAuthXBLNotFoundException;
import com.mcreater.amclcore.i18n.I18NManager;
import com.mcreater.amclcore.model.oauth.AuthCodeModel;
import com.mcreater.amclcore.model.oauth.DeviceCodeConverterModel;
import com.mcreater.amclcore.model.oauth.DeviceCodeModel;
import com.mcreater.amclcore.model.oauth.LoginDeviceCodeErrorType;
import com.mcreater.amclcore.model.oauth.MinecraftRequestModel;
import com.mcreater.amclcore.model.oauth.MinecraftResponseModel;
import com.mcreater.amclcore.model.oauth.TokenResponseModel;
import com.mcreater.amclcore.model.oauth.XBLTokenRequestModel;
import com.mcreater.amclcore.model.oauth.XBLTokenResponseModel;
import com.mcreater.amclcore.model.oauth.XBLUserModel;
import com.mcreater.amclcore.model.oauth.XSTSTokenResponseModel;
import com.mcreater.amclcore.util.HttpClientWrapper;
import com.mcreater.amclcore.util.SwingUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.mcreater.amclcore.concurrent.ConcurrentUtil.sleepTime;
import static com.mcreater.amclcore.util.JsonUtil.createList;
import static com.mcreater.amclcore.util.JsonUtil.createPair;
import static com.mcreater.amclcore.util.PropertyUtil.readProperty;

/**
 * OAuth Microsoft official <a href="https://learn.microsoft.com/zh-cn/azure/active-directory/develop/v2-oauth2-auth-code-flow">documentation</a><br>
 * Mojang minecraft auth <a href="https://wiki.vg/Mojang_API">API</a>
 */
@AllArgsConstructor
public class OAuth {
    /**
     * The microsoft oauth instance for {@link OAuth}
     */
    public static final OAuth MICROSOFT = new OAuth(
            "login.microsoftonline.com/consumers/oauth2/v2.0/devicecode",
            "login.microsoftonline.com/consumers/oauth2/v2.0/token",
            "login.live.com/oauth20_token.srf"
    );
    private final String deviceCodeUrl;
    private final String tokenUrl;
    private final String authTokenUrl;

    /**
     * AMCL/AMCLCore azure application id
     */
    @Getter
    private static final String defaultClientId = "1a969022-f24f-4492-a91c-6f4a6fcb373c";
    /**
     * client id override, using command line {@code -Damclcore.oauth.clientid.override=YOUR_CLIENTID}
     */
    @Getter
    public static final String clientIdPropertyName = "amclcore.oauth.clientid.override";
    /**
     * Minecraft azure application id
     */
    @Getter
    @Deprecated
    private static final String minecraftAzureApplicationId = "00000000402b5328";
    /**
     * Minecraft azure login url
     */
    @Getter
    private static final String minecraftAzureLoginUrl = "https://login.live.com/oauth20_authorize.srf?client_id=00000000402b5328&response_type=code&scope=service%3A%3Auser.auth.xboxlive.com%3A%3AMBI_SSL&redirect_uri=https%3A%2F%2Flogin.live.com%2Foauth20_desktop.srf";
    /**
     * Azure direct login url pattern.
     */
    @Getter
    private static final Pattern minecraftAzureUrlPattern = Pattern.compile("https://login\\.live\\.com/oauth20_desktop\\.srf\\?code=(?<code>.*)&lc=(?<lc>.*)");

    /**
     * XBox token api url
     */
    @Getter
    private static final String xblTokenUrl = "user.auth.xboxlive.com/user/authenticate";
    /**
     * XSTS validation url
     */
    @Getter
    private static final String xstsTokenUrl = "xsts.auth.xboxlive.com/xsts/authorize";

    /**
     * Default device code handler, copy the user code {@link DeviceCodeModel#getUserCode()} and open browser {@link DeviceCodeModel#getVerificationUri()}
     */
    @Getter
    private static final Consumer<DeviceCodeModel> defaultDevHandler = model2 -> ConcurrentExecutors.submitEx(
            ConcurrentExecutors.AWT_EVENT_EXECUTOR,
            SwingUtil.copyContentAsync(model2.getUserCode()),
            SwingUtil.openBrowserAsync(model2.getVerificationUri())
    );
    /**
     * the login url for XBox XSTS to Minecraft
     */
    @Getter
    private static final String mcLoginUrl = "api.minecraftservices.com/authentication/login_with_xbox";

    /**
     * Fetch device code model for auth
     *
     * @param requestHandler the handler for device token
     * @return the fetched device code model for next step {@link OAuth#checkToken(String)}
     * @throws URISyntaxException If the device code api url {@link OAuth#deviceCodeUrl} malformed
     * @throws IOException        If an I/O exception occurred
     */
    protected DeviceCodeModel fetchDeviceToken(Consumer<DeviceCodeModel> requestHandler) throws URISyntaxException, IOException {
        DeviceCodeModel model = HttpClientWrapper.create(HttpClientWrapper.Method.GET)
                .uri(deviceCodeUrl)
                .uriParam("client_id", createClientID())
                .uriParam("scope", buildScopeString("XboxLive.signin", "offline_access"))
                .timeout(5000)
                .reqTimeout(5000)
                .sendAndReadJson(DeviceCodeModel.class);

        Optional.of(requestHandler).ifPresent(c -> c.accept(model));
        return model;
    }

    /**
     * check the login state
     *
     * @param deviceCode the device code to be checked
     * @return the check result
     * @throws URISyntaxException If the device check api url is malformed
     * @throws IOException        If an I/O exception occurred
     */
    protected TokenResponseModel checkToken(String deviceCode) throws URISyntaxException, IOException {
        return HttpClientWrapper.create(HttpClientWrapper.Method.POST)
                .uri(tokenUrl)
                .entityEncodedUrl(
                        createPair("grant_type", buildScopeString2("urn", "ietf", "params", "oauth", "grant-type", "device_code")),
                        createPair("client_id", createClientID()),
                        createPair("code", deviceCode)
                )
                .timeout(5000)
                .reqTimeout(5000)
                .sendAndReadJson(TokenResponseModel.class);
    }

    /**
     * create token from auth code
     *
     * @param url the url from {@link OAuth#authTokenUrl}
     * @return the convert result
     * @throws URISyntaxException If the auth code api url is malformed
     * @throws IOException        If an I/O exception occurred
     */
    @Deprecated
    protected DeviceCodeConverterModel acquireAccessToken(String url) throws URISyntaxException, IOException {
        AuthCodeModel model = HttpClientWrapper.create(HttpClientWrapper.Method.GET)
                .uri(authTokenUrl)
                .entityEncodedUrl(
                        createPair("client_id", getMinecraftAzureApplicationId()),
                        createPair("code", parseRedirectUrl(url)),
                        createPair("grant_type", "authorization_code"),
                        createPair("redirect_uri", "https://login.live.com/oauth20_desktop.srf"),
                        createPair("scope", "service::user.auth.xboxlive.com::MBI_SSL")
                )
                .timeout(5000)
                .reqTimeout(5000)
                .sendAndReadJson(AuthCodeModel.class);

        return DeviceCodeConverterModel.builder()
                .model(
                        TokenResponseModel.builder()
                                .accessToken(model.getAccessToken())
                                .refreshToken(model.getRefreshToken())
                                .build()
                )
                .isDevice(false)
                .build();
    }

    /**
     * parse url from {@link OAuth#minecraftAzureUrlPattern} login
     *
     * @param rawUrl the redirect url
     * @return the parsed code
     */
    public String parseRedirectUrl(String rawUrl) {
        Matcher matcher = getMinecraftAzureUrlPattern().matcher(rawUrl);
        return matcher.find() ? matcher.group("code") : null;
    }

    /**
     * a wrapper for {@link OAuth#fetchDeviceToken(Consumer)}
     *
     * @param requestHandler the handler for device token
     * @return the fetched device code
     * @throws URISyntaxException If the xbox live api url is malformed
     * @throws IOException        If an I/O Exception occurred
     */
    protected DeviceCodeConverterModel detectUserCodeLoop(Consumer<DeviceCodeModel> requestHandler) throws URISyntaxException, IOException {
        DeviceCodeModel model = fetchDeviceToken(requestHandler);

        long startTime = System.nanoTime();
        int interval = model.getInterval();

        while (true) {
            sleepTime(Math.max(interval, 1));

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

            switch (LoginDeviceCodeErrorType.valueOf(checkIn.getError().toUpperCase())) {
                case AUTHORIZATION_PENDING:
                    continue;
                case SLOW_DOWN:
                    interval += 5;
                    continue;
                case EXPIRED_TOKEN:
                case INVALID_GRANT:
                default:
                    throw new OAuthTimeOutException();
            }
        }
    }

    /**
     * convert from access token to Xbox Live token
     *
     * @param parsedDeviceCode the verified access token
     * @return the fetched XBox Live token
     * @throws URISyntaxException If the xbox live api url is malformed
     * @throws IOException        If an I/O Exception occurred
     */
    protected XBLUserModel fetchXBLToken(DeviceCodeConverterModel parsedDeviceCode) throws IOException, URISyntaxException {
        XBLTokenRequestModel requestModel = HttpClientWrapper.create(HttpClientWrapper.Method.POST)
                .uri(getXblTokenUrl())
                .entityJson(
                        XBLTokenResponseModel.builder()
                                .Properties(
                                        XBLTokenResponseModel.XBLTokenResponsePropertiesModel.builder()
                                                .AuthMethod("RPS")
                                                .SiteName("user.auth.xboxlive.com")
                                                .RpsTicket(parsedDeviceCode.createAccessToken())
                                                .build()
                                )
                                .RelyingParty("http://auth.xboxlive.com")
                                .TokenType("JWT")
                )
                .sendAndReadJson(XBLTokenRequestModel.class);

        String userHash = requestModel.getDisplayClaims().getXui().stream()
                .map(XBLTokenRequestModel.XBLTokenUserHashModel::getUhs)
                .findAny()
                .orElseThrow(OAuthXBLNotFoundException::new);

        return XBLUserModel.builder()
                .token(requestModel.getToken())
                .hash(userHash)
                .build();
    }

    protected XBLUserModel fetchXSTSToken(XBLUserModel xblUser) throws IOException, URISyntaxException {
        XBLTokenRequestModel requestModel = HttpClientWrapper.create(HttpClientWrapper.Method.POST)
                .uri(getXstsTokenUrl())
                .entityJson(XSTSTokenResponseModel.builder()
                        .Properties(
                                XSTSTokenResponseModel.XSTSTokenResponsePropertiesModel.builder()
                                        .SandboxId("RETAIL")
                                        .UserTokens(createList(xblUser.getToken()))
                                        .build()
                        )
                        .RelyingParty("rp://api.minecraftservices.com/")
                        .TokenType("JWT")
                        .build()
                )
                .sendAndReadJson(XBLTokenRequestModel.class);

        String userHash = requestModel.getDisplayClaims().getXui().stream()
                .map(XBLTokenRequestModel.XBLTokenUserHashModel::getUhs)
                .findAny()
                .orElseThrow(OAuthUserHashException::new);

        if (!Objects.equals(userHash, xblUser.getHash())) throw new OAuthUserHashException();
        else return XBLUserModel.builder()
                .token(requestModel.getToken())
                .hash(userHash)
                .build();
    }

    protected MinecraftRequestModel fetchMinecraftToken(XBLUserModel xblUser) throws IOException, URISyntaxException {
        return HttpClientWrapper.create(HttpClientWrapper.Method.POST)
                .uri(getMcLoginUrl())
                .entityJson(MinecraftResponseModel.builder().identityToken(String.format("XBL3.0 x=%s;%s", xblUser.getHash(), xblUser.getToken())).build())
                .sendAndReadJson(MinecraftRequestModel.class);
    }

    /**
     * create a task for device token login
     *
     * @param requestHandler the handler for device token
     * @return created task
     */
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
        return readProperty(getClientIdPropertyName(), getDefaultClientId());
    }

    @AllArgsConstructor
    public class OAuthLoginTask extends AbstractTask<XBLUserModel, TaskStates.SimpleTaskStateWithArg<Integer>> {
        private final Consumer<DeviceCodeModel> requestHandler;

        public XBLUserModel call() throws Exception {
            DeviceCodeConverterModel deviceCode;
            // TODO fetch device code and login
            {
                setState(createTaskState(TaskStates.SimpleTaskStateWithArg.create(
                        I18NManager.get("core.oauth.deviceCode.pre.text"),
                        5
                )));
                deviceCode = detectUserCodeLoop(requestHandler);
                setState(createTaskState(TaskStates.SimpleTaskStateWithArg.create(
                        I18NManager.get("core.oauth.deviceCode.after.text"),
                        10
                )));
            }
            // TODO delegate task to login minecraft
            return ConcurrentExecutors.submit(
                    ConcurrentExecutors.OAUTH_LOGIN_EXECUTOR,
                    new OAuthLoginPartTask(deviceCode)
                            .addStateConsumer(t -> setState(
                                    TaskState.<TaskStates.SimpleTaskStateWithArg<Integer>, XBLUserModel>builder()
                                            .data(t.getData())
                                            .build()
                            ))
            ).get().orElseThrow(OAuthXBLNotFoundException::new);
        }
    }

    @AllArgsConstructor
    public class OAuthLoginPartTask extends AbstractTask<XBLUserModel, TaskStates.SimpleTaskStateWithArg<Integer>> {
        private final DeviceCodeConverterModel model;

        public XBLUserModel call() throws Exception {
            XBLUserModel xblToken, xstsToken;
            MinecraftRequestModel minecraftUser;
            // TODO login XBox Live
            {
                xblToken = fetchXBLToken(model);
                setState(createTaskState(TaskStates.SimpleTaskStateWithArg.create(
                        I18NManager.get("core.oauth.xbl.after.text"),
                        30
                )));
            }
            // TODO login XBox XSTS
            {
                xstsToken = fetchXSTSToken(xblToken);
                setState(createTaskState(TaskStates.SimpleTaskStateWithArg.create(
                        I18NManager.get("core.oauth.xsts.after.text"),
                        50
                )));
            }
            // TODO login minecraft (to be done)
            {
                minecraftUser = fetchMinecraftToken(xstsToken);
                setState(createTaskState(TaskStates.SimpleTaskStateWithArg.create(
                        I18NManager.get("core.oauth.mclogin.after.text"),
                        70
                )));
            }
            return xstsToken;
        }
    }
}
