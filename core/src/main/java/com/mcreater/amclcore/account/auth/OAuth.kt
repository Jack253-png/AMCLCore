package com.mcreater.amclcore.account.auth

import com.mcreater.amclcore.MetaData.Companion.getOauthDefaultClientId
import com.mcreater.amclcore.account.MicrosoftAccount
import com.mcreater.amclcore.account.MicrosoftAccount.Companion.create
import com.mcreater.amclcore.account.MicrosoftAccount.Companion.setApiAccessor
import com.mcreater.amclcore.concurrent.ConcurrentExecutors
import com.mcreater.amclcore.concurrent.TaskState
import com.mcreater.amclcore.concurrent.task.AbstractTask
import com.mcreater.amclcore.exceptions.oauth.OAuthMinecraftStoreCheckException
import com.mcreater.amclcore.exceptions.oauth.OAuthTimeOutException
import com.mcreater.amclcore.exceptions.oauth.OAuthUserHashException
import com.mcreater.amclcore.exceptions.oauth.OAuthXBLNotFoundException
import com.mcreater.amclcore.i18n.I18NManager.Companion.translatable
import com.mcreater.amclcore.i18n.Text
import com.mcreater.amclcore.model.oauth.*
import com.mcreater.amclcore.model.oauth.LoginDeviceCodeErrorType.Companion.parse
import com.mcreater.amclcore.model.oauth.XBLTokenResponseModel.XBLTokenResponsePropertiesModel
import com.mcreater.amclcore.model.oauth.XSTSTokenResponseModel.XSTSTokenResponsePropertiesModel
import com.mcreater.amclcore.util.HttpClientWrapper
import com.mcreater.amclcore.util.JsonUtil
import com.mcreater.amclcore.util.NetUtil
import com.mcreater.amclcore.util.SwingUtil.Companion.copyContentAsync
import com.mcreater.amclcore.util.SwingUtil.Companion.openBrowserAsync
import com.mcreater.amclcore.util.concurrent.ConcurrentUtil
import java.io.IOException
import java.net.URISyntaxException
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.function.Consumer
import kotlin.math.max
import kotlin.math.min

/**
 * OAuth Microsoft official [documentation](https://learn.microsoft.com/zh-cn/azure/active-directory/develop/v2-oauth2-auth-code-flow)<br></br>
 * Mojang minecraft auth [API](https://wiki.vg/Mojang_API)<br></br>
 * OAuth 微软官方 [文档](https://learn.microsoft.com/zh-cn/azure/active-directory/develop/v2-oauth2-auth-code-flow)<br></br>
 * * Mojang minecraft 登录验证 [API](https://wiki.vg/Mojang_API)
 */
enum class OAuth(private val deviceCodeUrl: String, private val tokenUrl: String) {
    /**
     * The microsoft oauth instance for [OAuth]<br></br>
     * [OAuth] 的微软登录验证实例
     */
    MICROSOFT(
        "login.microsoftonline.com/consumers/oauth2/v2.0/devicecode",
        "login.microsoftonline.com/consumers/oauth2/v2.0/token"
    );

    init {
        if ("MICROSOFT" == toString()) setApiAccessor(object : MicrosoftAccount.Accessor {
            override val minecraftProfileUrl: String
                get() = Companion.minecraftProfileUrl

            override fun createLoginInternalTask(model: DeviceCodeConverterModel?): OAuthLoginInternalTask {
                return OAuthLoginInternalTask(model!!)
            }

            override fun createClientID(): String {
                return getOauthDefaultClientId()
            }

            override val tokenUrl: String
                get() = this@OAuth.tokenUrl
            override val minecraftCapeModifyUrl: String
                get() = Companion.minecraftCapeModifyUrl
            override val minecraftNameCheckUrl: String
                get() = Companion.minecraftNameCheckUrl
            override val minecraftNameChangeUrl: String
                get() = Companion.minecraftNameChangeUrl
            override val minecraftNameChangeStateUrl: String
                get() = Companion.minecraftNameChangeStateUrl
            override val minecraftSkinResetUrl: String
                get() = Companion.minecraftSkinResetUrl
            override val minecraftSkinModifyUrl: String
                get() = Companion.minecraftSkinModifyUrl
        })
    }

    /**
     * Fetch device code model for auth<br></br>
     * 获取用于进行身份验证的设备码
     *
     * @param requestHandler the handler for device token<br></br>设备码处理器
     * @return the fetched device code model for next step [OAuth.checkToken]<br></br>已获取的设备码，用于下一步 [OAuth.checkToken]
     * @throws URISyntaxException If the device code api url [deviceCodeUrl] malformed<br></br>如果设备码API URL [deviceCodeUrl] 错误
     * @throws IOException        If an I/O exception occurred<br></br>如果一个IO错误发生
     */
    @Throws(URISyntaxException::class, IOException::class, NullPointerException::class)
    private fun fetchDeviceToken(requestHandler: Consumer<DeviceCodeModel>?): DeviceCodeModel {
        val model = HttpClientWrapper.create(HttpClientWrapper.Method.GET)
            .uri(deviceCodeUrl)
            .uriParam("client_id", getOauthDefaultClientId())
            .uriParam("scope", NetUtil.buildScopeString(" ", "XboxLive.signin", "offline_access"))
            .timeout(5000)
            .reqTimeout(5000)
            .retry(5)
            .sendAndReadJson(DeviceCodeModel::class.java)
        Optional.of(requestHandler!!).ifPresent { c: Consumer<DeviceCodeModel>? ->
            c!!.accept(
                model
            )
        }
        return model
    }

    /**
     * check the login state<br></br>
     * 检查登录状态
     *
     * @param deviceCode the device code to be checked<br></br>需要用来检查的设备码
     * @return the check result<br></br>检查结果
     * @throws URISyntaxException If the device check api url is malformed<br></br>如果设备码检查API URL 错误
     * @throws IOException        If an I/O exception occurred<br></br>如果一个IO错误发生
     */
    @Throws(URISyntaxException::class, IOException::class, NullPointerException::class)
    private fun checkToken(deviceCode: String?): TokenResponseModel {
        return HttpClientWrapper.create(HttpClientWrapper.Method.POST)
            .uri(tokenUrl)
            .entityEncodedUrl(
                JsonUtil.createPair(
                    "grant_type",
                    NetUtil.buildScopeString(":", "urn", "ietf", "params", "oauth", "grant-type", "device_code")
                ),
                JsonUtil.createPair("client_id", getOauthDefaultClientId()),
                JsonUtil.createPair("code", deviceCode)
            )
            .timeout(5000)
            .reqTimeout(5000)
            .sendAndReadJson(TokenResponseModel::class.java)
    }

    /**
     * detect user login by the result of [OAuth.fetchDeviceToken]<br></br>
     * 使用 [OAuth.fetchDeviceToken] 的结果检测用户登录
     *
     * @return the processed device code<br></br>被处理过的设备代码
     * @throws IOException If an I/O Exception occurred<br></br>如果一个IO错误发生
     */
    @Throws(IOException::class)
    private fun fetchUserLoginToken(model: DeviceCodeModel): DeviceCodeConverterModel {
        val startTime = System.nanoTime()
        var interval = model.interval
        while (true) {
            ConcurrentUtil.sleepTime(max(interval.toDouble(), 1.0).toLong())
            val estimatedTime = System.nanoTime() - startTime
            if (TimeUnit.SECONDS.convert(estimatedTime, TimeUnit.NANOSECONDS) >= min(
                    model.expiresIn.toDouble(),
                    900.0
                )
            ) {
                throw OAuthTimeOutException()
            }
            val checkIn: TokenResponseModel = try {
                checkToken(model.deviceCode)
            } catch (ignored: Exception) {
                continue
            }
            if (checkIn.error == null) return DeviceCodeConverterModel(checkIn, true)
            when (parse(checkIn.error!!.uppercase(Locale.getDefault()))) {
                LoginDeviceCodeErrorType.AUTHORIZATION_PENDING -> continue
                LoginDeviceCodeErrorType.SLOW_DOWN -> {
                    interval += 5
                    continue
                }

                LoginDeviceCodeErrorType.EXPIRED_TOKEN, LoginDeviceCodeErrorType.INVALID_GRANT -> throw OAuthTimeOutException()
                else -> throw OAuthTimeOutException()
            }
        }
    }

    /**
     * convert from access token to Xbox Live token<br></br>
     * 转换 Access 令牌到 XBox Live 令牌
     *
     * @param parsedDeviceCode the verified access token<br></br>已验证的 Access 令牌
     * @return the fetched XBox Live token<br></br>获取到的 XBox Live 令牌
     * @throws URISyntaxException If the xbox live api url is malformed<br></br>如果 XBox Live API 的 URL 错误
     * @throws IOException        If an I/O Exception occurred<br></br>如果一个IO错误发生
     */
    @Throws(IOException::class, URISyntaxException::class, NullPointerException::class)
    private fun fetchXBLUser(parsedDeviceCode: DeviceCodeConverterModel): XBLAccountModel {
        val (_, _, Token, DisplayClaims) = HttpClientWrapper.create(HttpClientWrapper.Method.POST)
            .uri(xblTokenUrl)
            .entityJson(
                XBLTokenResponseModel(
                    XBLTokenResponsePropertiesModel(
                        "RPS",
                        "user.auth.xboxlive.com",
                        parsedDeviceCode.createAccessToken()
                    ),
                    "http://auth.xboxlive.com",
                    "JWT"
                )
            )
            .retry(5)
            .sendAndReadJson(XBLTokenRequestModel::class.java)
        return XBLAccountModel(
            Token,
            DisplayClaims!!.xui!!.stream()
                .map(XBLTokenRequestModel.XBLTokenUserHashModel::uhs)
                .findAny()
                .orElseThrow { OAuthUserHashException() }
        )
    }

    /**
     * fetch XSTS user from XBox Live user<br></br>
     * 从 XBox Live 用户获取 XSTS 用户
     *
     * @param xblUser user from XBox Live [OAuth.fetchXBLUser]<br></br>从 [OAuth.fetchXBLUser] 得到的 XBox Live 用户
     * @return the fetched XSTS user<br></br>获取到的 XSTS 用户
     * @throws URISyntaxException If the XSTS api url is malformed<br></br>如果 XSTS API 的 URL 错误
     * @throws IOException        If an I/O Exception occurred<br></br>如果一个IO错误发生
     */
    @Throws(IOException::class, URISyntaxException::class, NullPointerException::class)
    private fun fetchXSTSUser(xblUser: XBLAccountModel): XBLAccountModel {
        val (_, _, Token, DisplayClaims) = HttpClientWrapper.create(HttpClientWrapper.Method.POST)
            .uri(xstsTokenUrl)
            .entityJson(
                XSTSTokenResponseModel(
                    XSTSTokenResponsePropertiesModel(
                        "RETAIL",
                        JsonUtil.createList(xblUser.token) as List<String>
                    ),
                    "rp://api.minecraftservices.com/",
                    "JWT"
                )
            )
            .retry(5)
            .sendAndReadJson(XBLTokenRequestModel::class.java)
        val userHash = DisplayClaims!!.xui!!.stream()
            .map(XBLTokenRequestModel.XBLTokenUserHashModel::uhs)
            .findAny()
            .orElseThrow { OAuthUserHashException() }
        return if (userHash != xblUser.hash) throw OAuthUserHashException() else XBLAccountModel(Token, userHash)
    }

    /**
     * login minecraft with XSTS user<br></br>
     * 从 XSTS 用户登录 Minecraft
     *
     * @param xblUser XSTS user from [OAuth.fetchXSTSUser]<br></br>从 [OAuth.fetchXSTSUser] 得到的 XSTS 用户
     * @return the login minecraft user<br></br>已登录的 Minecraft 用户
     * @throws URISyntaxException If the minecraft login api url is malformed<br></br>如果 Minecraft 登录API 的 URL 错误
     * @throws IOException        If an I/O Exception occurred<br></br>如果一个IO错误发生
     */
    @Throws(IOException::class, URISyntaxException::class, NullPointerException::class)
    private fun fetchMinecraftUser(xblUser: XBLAccountModel): MinecraftRequestModel {
        return HttpClientWrapper.create(HttpClientWrapper.Method.POST)
            .uri(minecraftLoginUrl)
            .entityJson(
                MinecraftResponseModel(
                    String.format(
                        "XBL3.0 x=%s;%s",
                        xblUser.hash,
                        xblUser.token
                    )
                )
            )
            .retry(5)
            .sendAndReadJson(MinecraftRequestModel::class.java)
    }

    /**
     * check Minecraft store state with [MinecraftRequestModel]<br></br>
     * 使用 [MinecraftRequestModel] 检查 Minecraft 商店状态
     *
     * @param user Minecraft user from [OAuth.fetchMinecraftUser]<br></br>从 [OAuth.fetchMinecraftUser] 得到的 Minecraft 用户
     * @return the check result<br></br>检查结果
     * @throws URISyntaxException If the minecraft store api url is malformed<br></br>如果 Minecraft 商店API 的 URL 错误
     * @throws IOException        If an I/O Exception occurred<br></br>如果一个IO错误发生
     */
    @Throws(URISyntaxException::class, IOException::class, NullPointerException::class)
    private fun checkMinecraftStore(user: MinecraftRequestModel): Boolean {
        val (items) = HttpClientWrapper.create(HttpClientWrapper.Method.GET)
            .uri(minecraftStoreUrl)
            .header("Authorization", String.format("%s %s", user.tokenType, user.accessToken))
            .retry(5)
            .sendAndReadJson(MinecraftProductRequestModel::class.java)
        return items!!.stream()
            .filter {
                "game_minecraft" == (it?.name ?: "") ||
                        "product_minecraft" == (it?.name ?: "")
            }
            .count() >= 2
    }

    /**
     * create a task for device token login<br></br>
     * 创建一个用于设备码登录的任务
     *
     * @param requestHandler the handler for device token<br></br>设备码的处理器
     * @return created task<br></br>创建的任务
     */
    fun deviceCodeLoginAsync(requestHandler: Consumer<DeviceCodeModel>?): OAuthLoginTask {
        return OAuthLoginTask(requestHandler)
    }

    inner class OAuthLoginTask(private val requestHandler: Consumer<DeviceCodeModel>? = null) :
        AbstractTask<MicrosoftAccount>() {
        @Throws(Exception::class)
        override fun call(): MicrosoftAccount {
            var deviceCodeRaw: DeviceCodeModel
            var deviceCode: DeviceCodeConverterModel
            // TODO fetch device code
            run {
                setState(
                    TaskState<MicrosoftAccount>(
                        taskType = TaskState.Type.EXECUTING,
                        totalStage = 3,
                        currentStage = 0,
                        message = translatable("core.oauth.login.start")
                    )
                )
                deviceCodeRaw = fetchDeviceToken(requestHandler)
            }
            // TODO let user login
            run {

                //
                setState(
                    TaskState<MicrosoftAccount>(
                        TaskState.Type.EXECUTING,
                        null,
                        null,
                        3, 1, translatable("core.oauth.deviceCode.pre.text")
                    )
                )
                deviceCode = fetchUserLoginToken(deviceCodeRaw)
            }
            // TODO fork & delegate internal task to login minecraft
            run {
                setState(
                    TaskState<MicrosoftAccount>(
                        TaskState.Type.EXECUTING,
                        null,
                        null,
                        3, 1, translatable("core.oauth.deviceCode.after.text")
                    )
                )
                return OAuthLoginInternalTask(deviceCode)
                    .bindTo(this)
                    .get()
                    .orElseThrow { OAuthXBLNotFoundException() } as MicrosoftAccount
            }
        }

        override fun getTaskName(): Text {
            return translatable("core.oauth.task.login.name")
        }
    }

    inner class OAuthLoginInternalTask(private val model: DeviceCodeConverterModel) :
        AbstractTask<MicrosoftAccount>() {
        @Throws(Exception::class)
        override fun call(): MicrosoftAccount {
            var xblToken: XBLAccountModel
            var xstsToken: XBLAccountModel
            var minecraftUser: MinecraftRequestModel
            var account: MicrosoftAccount
            // TODO login XBox Live
            run {
                xblToken = fetchXBLUser(model)
                setState(
                    TaskState<MicrosoftAccount>(
                        TaskState.Type.EXECUTING,
                        null,
                        null,
                        5, 1, translatable("core.oauth.xbl.after.text")
                    )
                )
            }
            // TODO login XBox XSTS
            run {
                xstsToken = fetchXSTSUser(xblToken)
                setState(
                    TaskState<MicrosoftAccount>(
                        TaskState.Type.EXECUTING,
                        null,
                        null,
                        5, 2, translatable("core.oauth.xsts.after.text")
                    )
                )
            }
            // TODO login minecraft and check account
            run {
                minecraftUser = fetchMinecraftUser(xstsToken)
                setState(
                    TaskState<MicrosoftAccount>(
                        TaskState.Type.EXECUTING,
                        null,
                        null,
                        5, 3, translatable("core.oauth.mclogin.after.text")
                    )
                )
            }
            // TODO check minecraft store and create instance of MicrosoftAccount
            run {
                if (!checkMinecraftStore(minecraftUser)) throw OAuthMinecraftStoreCheckException()
                account = create(minecraftUser, model)
                setState(
                    TaskState<MicrosoftAccount>(
                        TaskState.Type.EXECUTING,
                        null,
                        null,
                        5, 4, translatable("core.oauth.storeCheck.after.text")
                    )
                )
            }
            account.fetchProfileAsync()
                .bindTo(this)
                .get()
            return account
        }

        override fun getTaskName(): Text {
            return translatable("core.oauth.task.login.internal.name")
        }
    }

    companion object {
        /**
         * Minecraft azure login url<br></br>
         * Minecraft azure 登录URL
         */
        @Deprecated("")
        val minecraftAzureLoginUrl =
            "https://login.live.com/oauth20_authorize.srf?client_id=00000000402b5328&response_type=code&scope=service%3A%3Auser.auth.xboxlive.com%3A%3AMBI_SSL&redirect_uri=https%3A%2F%2Flogin.live.com%2Foauth20_desktop.srf"

        /**
         * XBox token api url<br></br>
         * XBox 令牌API URL
         */
        private const val xblTokenUrl = "user.auth.xboxlive.com/user/authenticate"

        /**
         * XSTS validation url<br></br>
         * XSTS 验证URL
         */
        private const val xstsTokenUrl = "xsts.auth.xboxlive.com/xsts/authorize"

        /**
         * Minecraft store url<br></br>
         * Minecraft 商店URL
         */
        private const val minecraftStoreUrl = "api.minecraftservices.com/entitlements/mcstore"

        /**
         * Default device code handler, copy the user code [DeviceCodeModel.deviceCode] and open browser [DeviceCodeModel.verificationUri]<br></br>
         * 默认设备码处理器, 复制从 [DeviceCodeModel.userCode] 得到的用户码并打开浏览器 [DeviceCodeModel.verificationUri]
         */
        val defaultDevHandler =
            Consumer { (userCode, _, verificationUri): DeviceCodeModel ->
                listOf(
                    copyContentAsync(userCode!!),
                    openBrowserAsync(verificationUri!!)
                ).forEach { ConcurrentExecutors.AWT_EVENT_EXECUTOR.execute(it) }
            }

        /**
         * the login url for XBox XSTS to Minecraft<br></br>
         * 从 XBox XSTS 登录至 Minecraft 的 URL
         */
        private const val minecraftLoginUrl = "api.minecraftservices.com/authentication/login_with_xbox"

        /**
         * the profile api url
         * 档案API URL
         */
        private const val minecraftProfileUrl = "api.minecraftservices.com/minecraft/profile"

        /**
         * the cape modify url for Minecraft<br></br>
         * Minecraft 披风修改 URL
         */
        private const val minecraftCapeModifyUrl = "api.minecraftservices.com/minecraft/profile/capes/active"

        /**
         * the profile name check url for Minecraft<br></br>
         * Minecraft 档案名检查 URL
         */
        private const val minecraftNameCheckUrl = "api.minecraftservices.com/minecraft/profile/name/%s/available"

        /**
         * the profile name change url for Minecraft<br></br>
         * Minecraft 档案名更改 URL
         */
        private const val minecraftNameChangeUrl = "api.minecraftservices.com/minecraft/profile/name/%s"

        /**
         * the profile name change state url for Minecraft<br></br>
         * Minecraft 档案名更改状态 URL
         */
        private const val minecraftNameChangeStateUrl = "api.minecraftservices.com/minecraft/profile/namechange"

        /**
         * the skin reset url for Minecraft<br></br>
         * Minecraft 皮肤重置 URL
         */
        private const val minecraftSkinResetUrl = "api.minecraftservices.com/minecraft/profile/skins/active"

        /**
         * the skin upload url for Minecraft<br></br>
         * Minecraft 皮肤上传 URL
         */
        private const val minecraftSkinModifyUrl = "api.minecraftservices.com/minecraft/profile/skins"
    }
}
