package com.mcreater.amclcore.account.auth;

import com.mcreater.amclcore.account.MicrosoftAccount;
import com.mcreater.amclcore.concurrent.ConcurrentExecutors;
import com.mcreater.amclcore.concurrent.TaskState;
import com.mcreater.amclcore.concurrent.task.AbstractTask;
import com.mcreater.amclcore.exceptions.oauth.OAuthMinecraftStoreCheckException;
import com.mcreater.amclcore.exceptions.oauth.OAuthTimeOutException;
import com.mcreater.amclcore.exceptions.oauth.OAuthUserHashException;
import com.mcreater.amclcore.exceptions.oauth.OAuthXBLNotFoundException;
import com.mcreater.amclcore.i18n.Text;
import com.mcreater.amclcore.model.oauth.*;
import com.mcreater.amclcore.util.HttpClientWrapper;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static com.mcreater.amclcore.MetaData.getOauthDefaultClientId;
import static com.mcreater.amclcore.i18n.I18NManager.translatable;
import static com.mcreater.amclcore.util.JsonUtil.createList;
import static com.mcreater.amclcore.util.JsonUtil.createPair;
import static com.mcreater.amclcore.util.NetUtil.buildScopeString;
import static com.mcreater.amclcore.util.SwingUtil.copyContentAsync;
import static com.mcreater.amclcore.util.SwingUtil.openBrowserAsync;
import static com.mcreater.amclcore.util.concurrent.ConcurrentUtil.sleepTime;
import static java.util.Objects.requireNonNull;

/**
 * OAuth Microsoft official <a href="https://learn.microsoft.com/zh-cn/azure/active-directory/develop/v2-oauth2-auth-code-flow">documentation</a><br>
 * Mojang minecraft auth <a href="https://wiki.vg/Mojang_API">API</a><br>
 * OAuth 微软官方 <a href="https://learn.microsoft.com/zh-cn/azure/active-directory/develop/v2-oauth2-auth-code-flow">文档</a><br>
 * * Mojang minecraft 登录验证 <a href="https://wiki.vg/Mojang_API">API</a>
 */
public enum OAuth {
    /**
     * The microsoft oauth instance for {@link OAuth}<br>
     * {@link OAuth} 的微软登录验证实例
     */
    MICROSOFT(
            "login.microsoftonline.com/consumers/oauth2/v2.0/devicecode",
            "login.microsoftonline.com/consumers/oauth2/v2.0/token"
    );

    OAuth(String deviceCodeUrl, String tokenUrl) {
        this.deviceCodeUrl = deviceCodeUrl;
        this.tokenUrl = tokenUrl;
        if ("MICROSOFT".equals(toString())) MicrosoftAccount.setApiAccessor(new MicrosoftAccount.Accessor() {
            @Override
            public String getMinecraftProfileUrl() {
                return minecraftProfileUrl;
            }

            @Override
            public OAuthLoginInternalTask createLoginInternalTask(DeviceCodeConverterModel model) {
                return new OAuthLoginInternalTask(model);
            }

            public String createClientID() {
                return getOauthDefaultClientId();
            }

            public String getTokenUrl() {
                return tokenUrl;
            }

            public String getMinecraftCapeModifyUrl() {
                return minecraftCapeModifyUrl;
            }

            public String getMinecraftNameCheckUrl() {
                return minecraftNameCheckUrl;
            }

            public String getMinecraftNameChangeUrl() {
                return minecraftNameChangeUrl;
            }

            public String getMinecraftNameChangeStateUrl() {
                return minecraftNameChangeStateUrl;
            }

            public String getMinecraftSkinResetUrl() {
                return minecraftSkinResetUrl;
            }

            public String getMinecraftSkinModifyUrl() {
                return minecraftSkinModifyUrl;
            }
        });
    }
    private final String deviceCodeUrl;
    private final String tokenUrl;
    /**
     * Minecraft azure login url<br>
     * Minecraft azure 登录URL
     */
    @Deprecated
    public static final String minecraftAzureLoginUrl = "https://login.live.com/oauth20_authorize.srf?client_id=00000000402b5328&response_type=code&scope=service%3A%3Auser.auth.xboxlive.com%3A%3AMBI_SSL&redirect_uri=https%3A%2F%2Flogin.live.com%2Foauth20_desktop.srf";

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
    private static final String minecraftLoginUrl = "api.minecraftservices.com/authentication/login_with_xbox";
    /**
     * the profile api url
     * 档案API URL
     */
    private static final String minecraftProfileUrl = "api.minecraftservices.com/minecraft/profile";
    /**
     * the cape modify url for Minecraft<br>
     * Minecraft 披风修改 URL
     */
    private static final String minecraftCapeModifyUrl = "api.minecraftservices.com/minecraft/profile/capes/active";
    /**
     * the profile name check url for Minecraft<br>
     * Minecraft 档案名检查 URL
     */
    private static final String minecraftNameCheckUrl = "api.minecraftservices.com/minecraft/profile/name/%s/available";
    /**
     * the profile name change url for Minecraft<br>
     * Minecraft 档案名更改 URL
     */
    private static final String minecraftNameChangeUrl = "api.minecraftservices.com/minecraft/profile/name/%s";
    /**
     * the profile name change state url for Minecraft<br>
     * Minecraft 档案名更改状态 URL
     */
    private static final String minecraftNameChangeStateUrl = "api.minecraftservices.com/minecraft/profile/namechange";
    /**
     * the skin reset url for Minecraft<br>
     * Minecraft 皮肤重置 URL
     */
    private static final String minecraftSkinResetUrl = "api.minecraftservices.com/minecraft/profile/skins/active";
    /**
     * the skin upload url for Minecraft<br>
     * Minecraft 皮肤上传 URL
     */
    private static final String minecraftSkinModifyUrl = "api.minecraftservices.com/minecraft/profile/skins";


    /**
     * Fetch device code model for auth<br>
     * 获取用于进行身份验证的设备码
     *
     * @param requestHandler the handler for device token<br>设备码处理器
     * @return the fetched device code model for next step {@link OAuth#checkToken(String)}<br>已获取的设备码，用于下一步 {@link OAuth#checkToken(String)}
     * @throws URISyntaxException If the device code api url {@link OAuth#deviceCodeUrl} malformed<br>如果设备码API URL {@link OAuth#deviceCodeUrl} 错误
     * @throws IOException        If an I/O exception occurred<br>如果一个IO错误发生
     */
    private DeviceCodeModel fetchDeviceToken(Consumer<DeviceCodeModel> requestHandler) throws URISyntaxException, IOException, NullPointerException {
        DeviceCodeModel model = HttpClientWrapper.create(HttpClientWrapper.Method.GET)
                .uri(deviceCodeUrl)
                .uriParam("client_id", getOauthDefaultClientId())
                .uriParam("scope", buildScopeString(" ", "XboxLive.signin", "offline_access"))
                .timeout(5000)
                .reqTimeout(5000)
                .retry(5)
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
    private TokenResponseModel checkToken(String deviceCode) throws URISyntaxException, IOException, NullPointerException {
        return HttpClientWrapper.create(HttpClientWrapper.Method.POST)
                .uri(tokenUrl)
                .entityEncodedUrl(
                        createPair("grant_type", buildScopeString(":", "urn", "ietf", "params", "oauth", "grant-type", "device_code")),
                        createPair("client_id", getOauthDefaultClientId()),
                        createPair("code", deviceCode)
                )
                .timeout(5000)
                .reqTimeout(5000)
                .sendAndReadJson(TokenResponseModel.class);
    }

    /**
     * detect user login by the result of {@link OAuth#fetchDeviceToken(Consumer)}<br>
     * 使用 {@link OAuth#fetchDeviceToken(Consumer)} 的结果检测用户登录
     *
     * @return the processed device code<br>被处理过的设备代码
     * @throws IOException If an I/O Exception occurred<br>如果一个IO错误发生
     */
    private DeviceCodeConverterModel fetchUserLoginToken(DeviceCodeModel model) throws IOException {
        long startTime = System.nanoTime();
        int interval = model.getInterval();

        while (true) {
            sleepTime(Math.max(interval, 1));

            long estimatedTime = System.nanoTime() - startTime;
            if (TimeUnit.SECONDS.convert(estimatedTime, TimeUnit.NANOSECONDS) >= Math.min(model.getExpiresIn(), 900)) {
                throw new OAuthTimeOutException();
            }
            TokenResponseModel checkIn;
            try {
                checkIn = checkToken(model.getDeviceCode());
            } catch (Exception ignored) {
                continue;
            }

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
    private XBLAccountModel fetchXBLUser(DeviceCodeConverterModel parsedDeviceCode) throws IOException, URISyntaxException, NullPointerException {
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
                .retry(5)
                .sendAndReadJson(XBLTokenRequestModel.class);

        return XBLAccountModel.builder()
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
     * @param xblUser user from XBox Live {@link OAuth#fetchXBLUser(DeviceCodeConverterModel)}<br>从 {@link OAuth#fetchXBLUser(DeviceCodeConverterModel)} 得到的 XBox Live 用户
     * @return the fetched XSTS user<br>获取到的 XSTS 用户
     * @throws URISyntaxException If the XSTS api url is malformed<br>如果 XSTS API 的 URL 错误
     * @throws IOException        If an I/O Exception occurred<br>如果一个IO错误发生
     */
    private XBLAccountModel fetchXSTSUser(XBLAccountModel xblUser) throws IOException, URISyntaxException, NullPointerException {
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
                .retry(5)
                .sendAndReadJson(XBLTokenRequestModel.class);

        String userHash = requestModel.getDisplayClaims().getXui().stream()
                .map(XBLTokenRequestModel.XBLTokenUserHashModel::getUhs)
                .findAny()
                .orElseThrow(OAuthUserHashException::new);

        if (!Objects.equals(userHash, xblUser.getHash())) throw new OAuthUserHashException();
        else return XBLAccountModel.builder()
                .token(requestModel.getToken())
                .hash(userHash)
                .build();
    }

    /**
     * login minecraft with XSTS user<br>
     * 从 XSTS 用户登录 Minecraft
     *
     * @param xblUser XSTS user from {@link OAuth#fetchXSTSUser(XBLAccountModel)}<br>从 {@link OAuth#fetchXSTSUser(XBLAccountModel)} 得到的 XSTS 用户
     * @return the login minecraft user<br>已登录的 Minecraft 用户
     * @throws URISyntaxException If the minecraft login api url is malformed<br>如果 Minecraft 登录API 的 URL 错误
     * @throws IOException        If an I/O Exception occurred<br>如果一个IO错误发生
     */
    private MinecraftRequestModel fetchMinecraftUser(XBLAccountModel xblUser) throws IOException, URISyntaxException, NullPointerException {
        return HttpClientWrapper.create(HttpClientWrapper.Method.POST)
                .uri(minecraftLoginUrl)
                .entityJson(MinecraftResponseModel.builder()
                        .identityToken(
                                String.format("XBL3.0 x=%s;%s",
                                        xblUser.getHash(),
                                        xblUser.getToken()
                                )
                        )
                        .build())
                .retry(5)
                .sendAndReadJson(MinecraftRequestModel.class);
    }

    /**
     * check Minecraft store state with {@link MinecraftRequestModel}<br>
     * 使用 {@link MinecraftRequestModel} 检查 Minecraft 商店状态
     *
     * @param user Minecraft user from {@link OAuth#fetchMinecraftUser(XBLAccountModel)}<br>从 {@link OAuth#fetchMinecraftUser(XBLAccountModel)} 得到的 Minecraft 用户
     * @return the check result<br>检查结果
     * @throws URISyntaxException If the minecraft store api url is malformed<br>如果 Minecraft 商店API 的 URL 错误
     * @throws IOException        If an I/O Exception occurred<br>如果一个IO错误发生
     */
    private boolean checkMinecraftStore(MinecraftRequestModel user) throws URISyntaxException, IOException, NullPointerException {
        MinecraftProductRequestModel requestModel = HttpClientWrapper.create(HttpClientWrapper.Method.GET)
                .uri(minecraftStoreUrl)
                .header("Authorization", String.format("%s %s", user.getTokenType(), user.getAccessToken()))
                .retry(5)
                .sendAndReadJson(MinecraftProductRequestModel.class);

        return requestModel.getItems().stream()
                .filter(m -> "game_minecraft".equals(m.getName()) || "product_minecraft".equals(m.getName()))
                .count() >= 2;
    }

    /**
     * create a task for device token login<br>
     * 创建一个用于设备码登录的任务
     *
     * @param requestHandler the handler for device token<br>设备码的处理器
     * @return created task<br>创建的任务
     */
    public OAuthLoginTask deviceCodeLoginAsync(Consumer<DeviceCodeModel> requestHandler) {
        return new OAuthLoginTask(requestHandler);
    }

    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public class OAuthLoginTask extends AbstractTask<MicrosoftAccount> {
        private final Consumer<DeviceCodeModel> requestHandler;

        protected MicrosoftAccount call() throws Exception {
            DeviceCodeModel deviceCodeRaw;
            DeviceCodeConverterModel deviceCode;
            // TODO fetch device code
            {
                setState(TaskState.<MicrosoftAccount>builder()
                        .totalStage(3)
                        .currentStage(0)
                        .message(translatable("core.oauth.login.start"))
                        .build());
                deviceCodeRaw = fetchDeviceToken(requestHandler);
            }
            // TODO let user login
            {
                setState(TaskState.<MicrosoftAccount>builder()
                        .totalStage(3)
                        .currentStage(1)
                        .message(translatable("core.oauth.deviceCode.pre.text"))
                        .build());
                deviceCode = fetchUserLoginToken(deviceCodeRaw);
            }
            // TODO fork & delegate internal task to login minecraft
            {
                setState(TaskState.<MicrosoftAccount>builder()
                        .totalStage(3)
                        .currentStage(2)
                        .message(translatable("core.oauth.deviceCode.after.text"))
                        .build());
                return new OAuthLoginInternalTask(deviceCode)
                        .bindTo(this)
                        .get()
                        .orElseThrow(OAuthXBLNotFoundException::new);
            }
        }

        protected Text getTaskName() {
            return translatable("core.oauth.task.login.name");
        }
    }

    public class OAuthLoginInternalTask extends AbstractTask<MicrosoftAccount> {
        private final DeviceCodeConverterModel model;

        private OAuthLoginInternalTask(@NotNull DeviceCodeConverterModel model) {
            this.model = requireNonNull(model);
        }

        protected MicrosoftAccount call() throws Exception {
            XBLAccountModel xblToken, xstsToken;
            MinecraftRequestModel minecraftUser;
            MicrosoftAccount account;
            // TODO login XBox Live
            {
                xblToken = fetchXBLUser(model);
                setState(TaskState.<MicrosoftAccount>builder()
                        .totalStage(5)
                        .currentStage(1)
                        .message(translatable("core.oauth.xbl.after.text"))
                        .build());
            }
            // TODO login XBox XSTS
            {
                xstsToken = fetchXSTSUser(xblToken);
                setState(TaskState.<MicrosoftAccount>builder()
                        .totalStage(5)
                        .currentStage(2)
                        .message(translatable("core.oauth.xsts.after.text"))
                        .build());
            }
            // TODO login minecraft and check account (to be done)
            {
                minecraftUser = fetchMinecraftUser(xstsToken);
                setState(TaskState.<MicrosoftAccount>builder()
                        .totalStage(5)
                        .currentStage(3)
                        .message(translatable("core.oauth.mclogin.after.text"))
                        .build());
            }
            // TODO check minecraft store and create instance of MicrosoftAccount
            {
                if (!checkMinecraftStore(minecraftUser)) throw new OAuthMinecraftStoreCheckException();
                account = MicrosoftAccount.create(minecraftUser, model);
                setState(TaskState.<MicrosoftAccount>builder()
                        .totalStage(5)
                        .currentStage(4)
                        .message(translatable("core.oauth.storeCheck.after.text"))
                        .build());
            }

            account.fetchProfileAsync()
                    .bindTo(this)
                    .get();

            return account;
        }

        protected Text getTaskName() {
            return translatable("core.oauth.task.login.internal.name");
        }
    }
}
