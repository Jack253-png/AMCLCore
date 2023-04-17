package com.mcreater.amclcore.account.auth;

import com.mcreater.amclcore.concurrent.AbstractTask;
import com.mcreater.amclcore.concurrent.ConcurrentExecutors;
import com.mcreater.amclcore.concurrent.TaskState;
import com.mcreater.amclcore.exceptions.oauth.OAuthTimeOutException;
import com.mcreater.amclcore.exceptions.oauth.OAuthUserHashException;
import com.mcreater.amclcore.exceptions.oauth.OAuthXBLNotFoundException;
import com.mcreater.amclcore.i18n.I18NManager;
import com.mcreater.amclcore.model.oauth.*;
import com.mcreater.amclcore.util.HttpClientWrapper;
import lombok.AllArgsConstructor;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.mcreater.amclcore.MetaData.oauthClientIdOverridePropertyName;
import static com.mcreater.amclcore.MetaData.oauthDefaultClientId;
import static com.mcreater.amclcore.concurrent.ConcurrentUtil.sleepTime;
import static com.mcreater.amclcore.util.JsonUtil.createList;
import static com.mcreater.amclcore.util.JsonUtil.createPair;
import static com.mcreater.amclcore.util.NetUtil.buildScopeString;
import static com.mcreater.amclcore.util.PropertyUtil.readProperty;
import static com.mcreater.amclcore.util.SwingUtil.copyContentAsync;
import static com.mcreater.amclcore.util.SwingUtil.openBrowserAsync;

/**
 * OAuth Microsoft official <a href="https://learn.microsoft.com/zh-cn/azure/active-directory/develop/v2-oauth2-auth-code-flow">documentation</a><br>
 * Mojang minecraft auth <a href="https://wiki.vg/Mojang_API">API</a><br>
 * OAuth 微软官方 <a href="https://learn.microsoft.com/zh-cn/azure/active-directory/develop/v2-oauth2-auth-code-flow">文档</a><br>
 * * Mojang minecraft 登录验证 <a href="https://wiki.vg/Mojang_API">API</a>
 */
@AllArgsConstructor
public enum OAuth {
    /**
     * The microsoft oauth instance for {@link OAuth}<br>
     * {@link OAuth} 的微软登录验证实例
     */
    MICROSOFT(
            "login.microsoftonline.com/consumers/oauth2/v2.0/devicecode",
            "login.microsoftonline.com/consumers/oauth2/v2.0/token",
            "login.live.com/oauth20_token.srf"
    );
    private final String deviceCodeUrl;
    private final String tokenUrl;
    private final String authTokenUrl;
    /**
     * Minecraft azure application id<br>
     * Minecraft azure 应用ID
     */
    @Deprecated
    private static final String minecraftAzureApplicationId = "00000000402b5328";
    /**
     * Minecraft azure login url<br>
     * Minecraft azure 登录URL
     */
    private static final String minecraftAzureLoginUrl = "https://login.live.com/oauth20_authorize.srf?client_id=00000000402b5328&response_type=code&scope=service%3A%3Auser.auth.xboxlive.com%3A%3AMBI_SSL&redirect_uri=https%3A%2F%2Flogin.live.com%2Foauth20_desktop.srf";
    /**
     * Azure direct login url pattern<br>
     * Azure 直接登录URL模板
     */
    private static final Pattern minecraftAzureUrlPattern = Pattern.compile("https://login\\.live\\.com/oauth20_desktop\\.srf\\?code=(?<code>.*)&lc=(?<lc>.*)");

    /**
     * XBox token api url<br>
     * XBox 令牌API URL
     */
    private static final String xblTokenUrl = "user.auth.xboxlive.com/user/authenticate";
    /**
     * XSTS validation url<br>
     * XSTS 验证URL
     */
    private static final String xstsTokenUrl = "xsts.auth.xboxlive.com/xsts/authorize";
    /**
     * Minecraft store url<br>
     * Minecraft 商店URL
     */
    private static final String minecraftStoreUrl = "api.minecraftservices.com/entitlements/mcstore";

    /**
     * Default device code handler, copy the user code {@link DeviceCodeModel#getUserCode()} and open browser {@link DeviceCodeModel#getVerificationUri()}<br>
     * 默认设备码处理器, 复制从 {@link DeviceCodeModel#getUserCode()} 得到的用户码并打开浏览器 {@link DeviceCodeModel#getVerificationUri()}
     */
    public static final Consumer<DeviceCodeModel> defaultDevHandler =
            model2 -> Arrays.asList(
                    copyContentAsync(model2.getUserCode()),
                    openBrowserAsync(model2.getVerificationUri())
            ).forEach(ConcurrentExecutors.AWT_EVENT_EXECUTOR::execute);
    /**
     * the login url for XBox XSTS to Minecraft<br>
     * 从 XBox XSTS 登录至 Minecraft 的 URL
     */
    private static final String mcLoginUrl = "api.minecraftservices.com/authentication/login_with_xbox";

    /**
     * Fetch device code model for auth<br>
     * 获取用于进行身份验证的设备码
     *
     * @param requestHandler the handler for device token<br>设备码处理器
     * @return the fetched device code model for next step {@link OAuth#checkToken(String)}<br>已获取的设备码，用于下一步 {@link OAuth#checkToken(String)}
     * @throws URISyntaxException If the device code api url {@link OAuth#deviceCodeUrl} malformed<br>如果设备码API URL {@link OAuth#deviceCodeUrl} 错误
     * @throws IOException        If an I/O exception occurred<br>如果一个IO错误发生
     */
    private DeviceCodeModel fetchDeviceToken(Consumer<DeviceCodeModel> requestHandler) throws URISyntaxException, IOException {
        DeviceCodeModel model = HttpClientWrapper.create(HttpClientWrapper.Method.GET)
                .uri(deviceCodeUrl)
                .uriParam("client_id", createClientID())
                .uriParam("scope", buildScopeString(" ", "XboxLive.signin", "offline_access"))
                .timeout(5000)
                .reqTimeout(5000)
                .sendAndReadJson(DeviceCodeModel.class);

        Optional.of(requestHandler).ifPresent(c -> c.accept(model));
        return model;
    }

    /**
     * check the login state<br>
     * 检查登录状态
     *
     * @param deviceCode the device code to be checked<br>需要用来检查的设备码
     * @return the check result<br>检查结果
     * @throws URISyntaxException If the device check api url is malformed<br>如果设备码检查API URL 错误
     * @throws IOException        If an I/O exception occurred<br>如果一个IO错误发生
     */
    private TokenResponseModel checkToken(String deviceCode) throws URISyntaxException, IOException {
        return HttpClientWrapper.create(HttpClientWrapper.Method.POST)
                .uri(tokenUrl)
                .entityEncodedUrl(
                        createPair("grant_type", buildScopeString(":", "urn", "ietf", "params", "oauth", "grant-type", "device_code")),
                        createPair("client_id", createClientID()),
                        createPair("code", deviceCode)
                )
                .timeout(5000)
                .reqTimeout(5000)
                .sendAndReadJson(TokenResponseModel.class);
    }

    /**
     * create token from auth code<br>
     * 从验证码创建令牌
     *
     * @param url the url from {@link OAuth#authTokenUrl}<br>从 {@link OAuth#authTokenUrl} 产生的URL
     * @return the convert result<br>转换结果
     * @throws URISyntaxException If the auth code api url is malformed<br>如果身份验证API URL 错误
     * @throws IOException        If an I/O exception occurred<br>如果一个IO错误发生
     */
    @Deprecated
    private DeviceCodeConverterModel acquireAccessToken(String url) throws URISyntaxException, IOException {
        AuthCodeModel model = HttpClientWrapper.create(HttpClientWrapper.Method.GET)
                .uri(authTokenUrl)
                .entityEncodedUrl(
                        createPair("client_id", minecraftAzureApplicationId),
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
     * parse url from {@link OAuth#minecraftAzureUrlPattern} login<br>
     * 解析从 {@link OAuth#minecraftAzureUrlPattern} 登录产生的URL
     *
     * @param rawUrl the redirect url<br>重定向URL
     * @return the parsed code<br>已解析的代码
     */
    public String parseRedirectUrl(String rawUrl) {
        Matcher matcher = minecraftAzureUrlPattern.matcher(rawUrl);
        return matcher.find() ? matcher.group("code") : null;
    }

    /**
     * detect user login by the result of {@link OAuth#fetchDeviceToken(Consumer)}<br>
     * 使用 {@link OAuth#fetchDeviceToken(Consumer)} 的结果检测用户登录
     *
     * @return the processed device code<br>被处理过的设备代码
     * @throws URISyntaxException If the xbox live api url is malformed<br>如果XBox Live API 的 URL 错误
     * @throws IOException        If an I/O Exception occurred<br>如果一个IO错误发生
     */
    private DeviceCodeConverterModel fetchUserLoginToken(DeviceCodeModel model) throws URISyntaxException, IOException {
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

            switch (LoginDeviceCodeErrorType.parse(checkIn.getError().toUpperCase())) {
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
     * convert from access token to Xbox Live token<br>
     * 转换 Access 令牌到 XBox Live 令牌
     *
     * @param parsedDeviceCode the verified access token<br>已验证的 Access 令牌
     * @return the fetched XBox Live token<br>获取到的 XBox Live 令牌
     * @throws URISyntaxException If the xbox live api url is malformed<br>如果 XBox Live API 的 URL 错误
     * @throws IOException        If an I/O Exception occurred<br>如果一个IO错误发生
     */
    private XBLUserModel fetchXBLToken(DeviceCodeConverterModel parsedDeviceCode) throws IOException, URISyntaxException {
        XBLTokenRequestModel requestModel = HttpClientWrapper.create(HttpClientWrapper.Method.POST)
                .uri(xblTokenUrl)
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

        return XBLUserModel.builder()
                .token(requestModel.getToken())
                .hash(
                        requestModel.getDisplayClaims().getXui().stream()
                                .map(XBLTokenRequestModel.XBLTokenUserHashModel::getUhs)
                                .findAny()
                                .orElseThrow(OAuthUserHashException::new)
                )
                .build();
    }

    /**
     * fetch XSTS user from XBox Live user<br>
     * 从 XBox Live 用户获取 XSTS 用户
     *
     * @param xblUser user from XBox Live {@link OAuth#fetchXBLToken(DeviceCodeConverterModel)}<br>从 {@link OAuth#fetchXBLToken(DeviceCodeConverterModel)} 得到的 XBox Live 用户
     * @return the fetched XSTS user<br>获取到的 XSTS 用户
     * @throws URISyntaxException If the XSTS api url is malformed<br>如果 XSTS API 的 URL 错误
     * @throws IOException        If an I/O Exception occurred<br>如果一个IO错误发生
     */
    private XBLUserModel fetchXSTSToken(XBLUserModel xblUser) throws IOException, URISyntaxException {
        XBLTokenRequestModel requestModel = HttpClientWrapper.create(HttpClientWrapper.Method.POST)
                .uri(xstsTokenUrl)
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

    /**
     * login minecraft with XSTS user<br>
     * 从 XSTS 用户登录 Minecraft
     *
     * @param xblUser XSTS user from {@link OAuth#fetchXSTSToken(XBLUserModel)}<br>从 {@link OAuth#fetchXSTSToken(XBLUserModel)} 得到的 XSTS 用户
     * @return the login minecraft user<br>已登录的 Minecraft 用户
     * @throws URISyntaxException If the minecraft login api url is malformed<br>如果 Minecraft 登录API 的 URL 错误
     * @throws IOException        If an I/O Exception occurred<br>如果一个IO错误发生
     */
    private MinecraftRequestModel fetchMinecraftToken(XBLUserModel xblUser) throws IOException, URISyntaxException {
        return HttpClientWrapper.create(HttpClientWrapper.Method.POST)
                .uri(mcLoginUrl)
                .entityJson(MinecraftResponseModel.builder()
                        .identityToken(
                                String.format("XBL3.0 x=%s;%s",
                                        xblUser.getHash(),
                                        xblUser.getToken()
                                )
                        )
                        .build())
                .sendAndReadJson(MinecraftRequestModel.class);
    }

    private boolean checkMinecraftStore(MinecraftRequestModel user) throws URISyntaxException, IOException {
        MinecraftProductRequestModel requestModel = HttpClientWrapper.create(HttpClientWrapper.Method.GET)
                .uri(minecraftStoreUrl)
                .header("Authorization", String.format("%s %s", user.getTokenType(), user.getAccessToken()))
                .sendAndReadJson(MinecraftProductRequestModel.class);
        System.out.println(requestModel);
        return false;
    }

    /**
     * create a task for device token login<br>
     * 创建一个用于设备码登录的任务
     * @param requestHandler the handler for device token<br>设备码的处理器
     * @return created task<br>创建的任务
     */
    public OAuthLoginTask fetchDeviceTokenAsync(Consumer<DeviceCodeModel> requestHandler) {
        return new OAuthLoginTask(requestHandler);
    }

    private static String createClientID() {
        return readProperty(oauthClientIdOverridePropertyName, oauthDefaultClientId);
    }

    @AllArgsConstructor
    public class OAuthLoginTask extends AbstractTask<MinecraftRequestModel> {
        private final Consumer<DeviceCodeModel> requestHandler;

        public MinecraftRequestModel call() throws Exception {
            DeviceCodeModel deviceCodeRaw;
            DeviceCodeConverterModel deviceCode;
            // TODO fetch device code
            {
                setState(TaskState.<MinecraftRequestModel>builder()
                        .totalStage(7)
                        .currentStage(0)
                        .message(I18NManager.translatable("core.oauth.login.start"))
                        .build());
                deviceCodeRaw = fetchDeviceToken(requestHandler);
            }
            // TODO let user login
            {
                setState(TaskState.<MinecraftRequestModel>builder()
                        .totalStage(7)
                        .currentStage(1)
                        .message(I18NManager.translatable("core.oauth.deviceCode.pre.text"))
                        .build());
                deviceCode = fetchUserLoginToken(deviceCodeRaw);
            }
            // TODO fork & delegate internal task to login minecraft
            {
                setState(TaskState.<MinecraftRequestModel>builder()
                        .totalStage(7)
                        .currentStage(2)
                        .message(I18NManager.translatable("core.oauth.deviceCode.after.text"))
                        .build());
                return new OAuthLoginPartTask(deviceCode)
                        .bindTo(this)
                        .fork()
                        .get()
                        .orElseThrow(OAuthXBLNotFoundException::new);
            }
        }
    }

    @AllArgsConstructor
    protected class OAuthLoginPartTask extends AbstractTask<MinecraftRequestModel> {
        private final DeviceCodeConverterModel model;

        public MinecraftRequestModel call() throws Exception {
            XBLUserModel xblToken, xstsToken;
            MinecraftRequestModel minecraftUser;
            // TODO login XBox Live
            {
                xblToken = fetchXBLToken(model);
                setTopTaskState(TaskState.<MinecraftRequestModel>builder()
                        .totalStage(7)
                        .currentStage(3)
                        .message(I18NManager.translatable("core.oauth.xbl.after.text"))
                        .build());
                setState(TaskState.<MinecraftRequestModel>builder()
                        .totalStage(5)
                        .currentStage(1)
                        .message(I18NManager.translatable("core.oauth.xbl.after.text"))
                        .build());
            }
            // TODO login XBox XSTS
            {
                xstsToken = fetchXSTSToken(xblToken);
                setTopTaskState(TaskState.<MinecraftRequestModel>builder()
                        .totalStage(7)
                        .currentStage(4)
                        .message(I18NManager.translatable("core.oauth.xsts.after.text"))
                        .build());
                setState(TaskState.<MinecraftRequestModel>builder()
                        .totalStage(5)
                        .currentStage(2)
                        .message(I18NManager.translatable("core.oauth.xsts.after.text"))
                        .build());
            }
            // TODO login minecraft and check account (to be done)
            {
                minecraftUser = fetchMinecraftToken(xstsToken);
                setTopTaskState(TaskState.<MinecraftRequestModel>builder()
                        .totalStage(7)
                        .currentStage(5)
                        .message(I18NManager.translatable("core.oauth.mclogin.after.text"))
                        .build());
                setState(TaskState.<MinecraftRequestModel>builder()
                        .totalStage(5)
                        .currentStage(3)
                        .message(I18NManager.translatable("core.oauth.mclogin.after.text"))
                        .build());
            }
            {
                checkMinecraftStore(minecraftUser);
            }
            return minecraftUser;
        }
    }
}
