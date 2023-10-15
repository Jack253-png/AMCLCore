package com.mcreater.amclcore.account

import com.mcreater.amclcore.account.auth.OAuth.OAuthLoginInternalTask
import com.mcreater.amclcore.command.CommandArg
import com.mcreater.amclcore.concurrent.TaskState
import com.mcreater.amclcore.concurrent.task.AbstractAction
import com.mcreater.amclcore.concurrent.task.AbstractTask
import com.mcreater.amclcore.concurrent.task.model.RunnableAction
import com.mcreater.amclcore.exceptions.oauth.OAuthMinecraftNameChangeNotAllowedException
import com.mcreater.amclcore.exceptions.oauth.OAuthMinecraftNameConflictException
import com.mcreater.amclcore.exceptions.oauth.OAuthXBLNotFoundException
import com.mcreater.amclcore.i18n.I18NManager.Companion.translatable
import com.mcreater.amclcore.i18n.Text
import com.mcreater.amclcore.model.oauth.DeviceCodeConverterModel
import com.mcreater.amclcore.model.oauth.MinecraftRequestModel
import com.mcreater.amclcore.model.oauth.TokenResponseModel
import com.mcreater.amclcore.model.oauth.session.MinecraftEnableCapeResponseModel
import com.mcreater.amclcore.model.oauth.session.MinecraftNameChangeableRequestModel
import com.mcreater.amclcore.model.oauth.session.MinecraftNameChangedTimeRequestModel
import com.mcreater.amclcore.model.oauth.session.MinecraftProfileRequestModel
import com.mcreater.amclcore.model.oauth.session.MinecraftProfileRequestModel.MinecraftProfileCapeModel
import com.mcreater.amclcore.model.oauth.session.MinecraftProfileRequestModel.MinecraftProfileSkinModel
import com.mcreater.amclcore.util.FunctionUtil
import com.mcreater.amclcore.util.HttpClientWrapper
import com.mcreater.amclcore.util.ImageUtil
import com.mcreater.amclcore.util.JsonUtil
import com.mcreater.amclcore.util.date.DateUtil
import com.mcreater.amclcore.util.date.StandardDate
import org.apache.commons.lang3.tuple.ImmutablePair
import org.apache.http.entity.mime.MultipartEntityBuilder
import java.io.File
import java.io.IOException
import java.time.Instant
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.*
import java.util.regex.Pattern

/**
 * API Documentation<br></br>API 文档<br></br>
 * [Link<br></br>链接](https://wiki.vg/Mojang_API)
 */
class MicrosoftAccount : AbstractAccount {
    interface Accessor {
        val minecraftProfileUrl: String?

        fun createLoginInternalTask(model: DeviceCodeConverterModel?): OAuthLoginInternalTask
        fun createClientID(): String?
        val tokenUrl: String?
        val minecraftCapeModifyUrl: String?
        val minecraftNameCheckUrl: String?
        val minecraftNameChangeUrl: String?
        val minecraftNameChangeStateUrl: String?
        val minecraftSkinResetUrl: String?
        val minecraftSkinModifyUrl: String?

        companion object {
            val INSTANCE: Accessor = object : Accessor {
                override val minecraftProfileUrl: String?
                    get() {
                        throw UnsupportedOperationException("not implemented yet")
                    }

                override fun createLoginInternalTask(model: DeviceCodeConverterModel?): OAuthLoginInternalTask {
                    throw UnsupportedOperationException("not implemented yet")
                }

                override fun createClientID(): String {
                    throw UnsupportedOperationException("not implemented yet")
                }

                override val tokenUrl: String?
                    get() {
                        throw UnsupportedOperationException("not implemented yet")
                    }
                override val minecraftCapeModifyUrl: String?
                    get() {
                        throw UnsupportedOperationException("not implemented yet")
                    }
                override val minecraftNameCheckUrl: String?
                    get() {
                        throw UnsupportedOperationException("not implemented yet")
                    }
                override val minecraftNameChangeUrl: String?
                    get() {
                        throw UnsupportedOperationException("not implemented yet")
                    }
                override val minecraftNameChangeStateUrl: String?
                    get() {
                        throw UnsupportedOperationException("not implemented yet")
                    }
                override val minecraftSkinResetUrl: String?
                    get() {
                        throw UnsupportedOperationException("not implemented yet")
                    }
                override val minecraftSkinModifyUrl: String?
                    get() {
                        throw UnsupportedOperationException("not implemented yet")
                    }
            }
        }
    }

    var refreshToken: String?
    val tokenType: String?
    private var profile: MinecraftProfileRequestModel? = null
    private var isBasicInited: Boolean = false

    private constructor(
        minecraftUser: MinecraftRequestModel,
        refreshToken: String
    ) : super((minecraftUser.accessToken)!!) {
        this.refreshToken = refreshToken
        tokenType = minecraftUser.tokenType
    }

    private constructor(accessToken: String, refreshToken: String, tokenType: String) : super(accessToken) {
        this.refreshToken = refreshToken
        this.tokenType = tokenType
    }

    fun initBasicProfile(name: String?, uuid: UUID?) {
        if ((profile == null) && (name != null) && (uuid != null)) profile = MinecraftProfileRequestModel(
            uuid, name, null, null, null, null, null, null, null, null, null
        )
        isBasicInited = true
    }

    fun needFetchProfile(): Boolean {
        return profile == null || isBasicInited
    }

    /**
     * create account refresh task<br></br>
     * 创建账户刷新任务
     *
     * @return the created task<br></br>被创建的任务
     */
    override fun refreshAsync(): AbstractAction {
        return RefreshAccountTask()
    }

    /**
     * create fetch profile task<br></br>
     * 创建档案获取任务
     *
     * @return the created task<br></br>被创建的任务
     */
    override fun fetchProfileAsync(): FetchProfileTask {
        return FetchProfileTask()
    }

    /**
     * create check profile's AccessToken is outdated or not task<br></br>
     * 创建检查账户 AccessToken 是否失效与否的任务
     *
     * @return the created task<br></br>被创建的任务
     */
    override fun validateAccountAsync(): ValidateAccountTask {
        return ValidateAccountTask()
    }

    /**
     * create disable cape task<br></br>
     * 创建禁用披风任务
     *
     * @return the created task<br></br>被创建的任务
     */
    override fun disableAccountCapeAsync(): DisableAccountCapeTask {
        return DisableAccountCapeTask()
    }

    /**
     * create enable selected cape task<br></br>
     * 创建启用选定披风任务
     *
     * @param id the cape index<br></br>披风序列号
     * @return the created task<br></br>被创建的任务
     */
    override fun enableAccountCapeAsync(id: String?): EnableAccountCapeTask {
        return EnableAccountCapeTask(id)
    }

    /**
     * create check account name changeable task<br></br>
     * 创建检查账户名是否可以改变的任务
     *
     * @return the created task<br></br>被创建的任务
     */
    override fun checkAccountNameChangeableAsync(newName: String): NameChangeableCheckTask {
        return NameChangeableCheckTask(newName)
    }

    /**
     * create change account name task<br></br>
     * 创建更改账户名任务
     *
     * @param newName the created task<br></br>被创建的任务
     */
    override fun changeAccountNameAsync(newName: String): NameChangeTask {
        return NameChangeTask(newName)
    }

    /**
     * create check account name changed time task<br></br>
     * 创建检查账户名改变时间的任务
     *
     * @return the created task<br></br>被创建的任务
     */
    override fun checkAccountNameChangedTimeAsync(): AccountNameChangedTimeCheckTask {
        return AccountNameChangedTimeCheckTask()
    }

    /**
     * check account name is allowed<br></br>
     * 检查账户名是否允许
     * rule: letter / number / underline (_) / length <= 16<br></br>
     * 规则: 大小写字母 / 数字 / 下划线(_) / 长度 <= 16
     *
     * @return the check result<br></br>检查结果
     */
    override fun accountNameAllowed(name: String?): Boolean {
        if (name!!.length > 16) return false
        return !NAME_PATTERN.asPredicate().test(name)
    }

    /**
     * create reset skin task<br></br>
     * 创建重置皮肤任务
     *
     * @return the created task<br></br>被创建的任务
     */
    override fun resetSkinAsync(): ResetSkinTask {
        return ResetSkinTask()
    }

    /**
     * create upload skin task<br></br>
     * 创建上传皮肤任务
     *
     * @return the created task<br></br>被创建的任务
     */
    override fun uploadSkinAsync(file: File?, isSlim: Boolean): UploadSkinTask {
        return UploadSkinTask(file, isSlim)
    }

    override val addonArgs: List<CommandArg>
        get() = Vector()

    override fun preLaunchAsync(): RunnableAction? {
        return RunnableAction.of({}, translatable("core.game.launch.pre"))
    }

    val skins: List<MinecraftProfileSkinModel>?
        get() = Optional.ofNullable(profile)
            .map(MinecraftProfileRequestModel::skins)
            .orElse(Vector())
    val capes: List<MinecraftProfileCapeModel>?
        get() {
            return Optional.ofNullable(profile)
                .map(MinecraftProfileRequestModel::capes)
                .orElse(Vector())
        }
    override var accountName: String? = null
        get() {
            return Optional.ofNullable<MinecraftProfileRequestModel>(profile)
                .map<String?>(MinecraftProfileRequestModel::name)
                .map(FunctionUtil.genSelfFunction { accountName = field })
                .orElse(null)
        }
        set(accountName) {
            super.accountName = accountName
            field = accountName
        }
    override var uuid: UUID? = null
        get() {
            return Optional.ofNullable<MinecraftProfileRequestModel>(profile)
                .map<UUID?>(MinecraftProfileRequestModel::id)
                .map(FunctionUtil.genSelfFunction { uuid = field })
                .orElse(null)
        }
        set(uuid) {
            super.uuid = uuid
            field = uuid
        }

    inner class FetchProfileTask : AbstractAction() {
        override fun getTaskName(): Text {
            return translatable("core.oauth.task.fetchProfile.name")
        }

        @Throws(Exception::class)
        public override fun execute() {
            profile = HttpClientWrapper.create(HttpClientWrapper.Method.GET)
                .uri(apiAccessor.minecraftProfileUrl)
                .header(tokenHeader())
                .retry(5)
                .sendAndReadJson(MinecraftProfileRequestModel::class.java)
        }
    }

    inner class RefreshAccountTask : AbstractAction() {
        @Throws(Exception::class)
        override fun execute() {
            var model: TokenResponseModel
            // TODO Refresh with RefreshToken
            run {
                setState(
                    TaskState(
                        taskType = TaskState.Type.EXECUTING,
                        totalStage = 5,
                        currentStage = 1,
                        message = translatable("core.oauth.refreshAccount.pre.text")
                    )
                )
                model = HttpClientWrapper.create(HttpClientWrapper.Method.POST)
                    .uri(apiAccessor.tokenUrl)
                    .entityEncodedUrl(
                        JsonUtil.createPair("client_id", apiAccessor.createClientID()),
                        JsonUtil.createPair("refresh_token", refreshToken),
                        JsonUtil.createPair("grant_type", "refresh_token")
                    )
                    .timeout(5000)
                    .reqTimeout(5000)
                    .retry(5)
                    .sendAndReadJson(TokenResponseModel::class.java)
            }
            // TODO Update refresh token
            run {
                setState(
                    TaskState(
                        taskType = TaskState.Type.EXECUTING,
                        totalStage = 5,
                        currentStage = 2,
                        message = translatable("core.oauth.refreshAccount.refreshingToken.text")
                    )
                )
                refreshToken = model.refreshToken
            }
            // TODO Fork internal task for fetching AccessToken
            run {
                setState(
                    TaskState(
                        taskType = TaskState.Type.EXECUTING,
                        totalStage = 5,
                        currentStage = 3,
                        message = translatable("core.oauth.refreshAccount.refreshingBase.text")
                    )
                )
                val accountNew: MicrosoftAccount? = apiAccessor.createLoginInternalTask(
                    DeviceCodeConverterModel(model, true)
                )
                    .bindTo(this)
                    .get()
                    .orElseThrow { OAuthXBLNotFoundException() }
                accessToken = accountNew?.accessToken
            }
            // TODO Update profile
            run {
                setState(
                    TaskState(
                        taskType = TaskState.Type.EXECUTING,
                        totalStage = 5,
                        currentStage = 4,
                        message = translatable("core.oauth.refreshAccount.updateProfile.text")
                    )
                )
                fetchProfileAsync()
                    .bindTo(this)
                    .get()
            }
        }

        override fun getTaskName(): Text {
            return translatable("core.oauth.task.refreshAccount.name")
        }
    }

    inner class ValidateAccountTask : AbstractTask<Boolean?>() {
        override fun call(): Boolean {
            return try {
                fetchProfileAsync().execute()
                true
            } catch (e: Exception) {
                false
            }
        }

        override fun getTaskName(): Text {
            return translatable("core.oauth.task.validate.text")
        }
    }

    inner class DisableAccountCapeTask : AbstractAction() {
        @Throws(Exception::class)
        override fun execute() {
            profile = HttpClientWrapper.create(HttpClientWrapper.Method.DELETE)
                .uri(apiAccessor.minecraftCapeModifyUrl)
                .header(tokenHeader())
                .timeout(5000)
                .reqTimeout(5000)
                .retry(5)
                .sendAndReadJson(MinecraftProfileRequestModel::class.java)
        }

        override fun getTaskName(): Text {
            return translatable("core.oauth.task.disable_cape.text")
        }
    }

    private fun tokenHeader(): Map.Entry<String, String> {
        return ImmutablePair("Authorization", String.format("%s %s", tokenType, accessToken))
    }

    inner class NameChangeableCheckTask(private val newName: String? = null) : AbstractTask<Boolean?>() {
        @Throws(Exception::class)
        override fun call(): Boolean {
            return HttpClientWrapper.create(HttpClientWrapper.Method.GET)
                .uri(String.format((apiAccessor.minecraftNameCheckUrl)!!, newName))
                .header(tokenHeader())
                .timeout(5000)
                .reqTimeout(5000)
                .retry(5)
                .sendAndReadJson(MinecraftNameChangeableRequestModel::class.java)
                .status === MinecraftNameChangeableRequestModel.State.AVAILABLE
        }

        override fun getTaskName(): Text {
            return translatable("core.oauth.task.name_changeable_check.text")
        }
    }

    inner class NameChangeTask(private val newName: String? = null) : AbstractAction() {
        @Throws(Exception::class)
        override fun execute() {
            val changeable: Boolean = (checkAccountNameChangeableAsync((newName)!!)
                .bindTo(this)
                .get()
                ?.orElse(false))!!
            val model: Optional<MinecraftNameChangedTimeRequestModel?>? = checkAccountNameChangedTimeAsync()
                .bindTo(this)
                .get()
            val curr: Date = Date.from(
                model?.map { it?.changedAt }
                    ?.orElse(StandardDate.DEFAULT)
                    ?.convert()
                    ?.atZone(ZoneId.systemDefault())
                    ?.toInstant() ?: Instant.now()
            )
            val next: Date = Date.from(
                curr.toInstant()
                    .plus(30, ChronoUnit.DAYS)
                    .atZone(ZoneId.systemDefault())
                    .toInstant()
            )
            val system: Date = Date.from(
                Instant.now()
                    .atZone(ZoneId.systemDefault())
                    .toInstant()
            )
            if (!changeable) throw OAuthMinecraftNameConflictException()
            if (!model?.map { it?.nameChangeAllowed == true }?.orElse(false)!!
            ) throw OAuthMinecraftNameChangeNotAllowedException(
                DateUtil.toDate(curr),
                DateUtil.toDate(next),
                DateUtil.dateBetween(next, system)
            )
            profile = HttpClientWrapper.create(HttpClientWrapper.Method.PUT)
                .uri(String.format((apiAccessor.minecraftNameChangeUrl)!!, newName))
                .header(tokenHeader())
                .timeout(5000)
                .reqTimeout(5000)
                .retry(5)
                .sendAndReadJson(MinecraftProfileRequestModel::class.java)
        }

        override fun getTaskName(): Text {
            return translatable("core.oauth.task.changeName.text")
        }
    }

    inner class AccountNameChangedTimeCheckTask :
        AbstractTask<MinecraftNameChangedTimeRequestModel?>() {
        @Throws(Exception::class)
        override fun call(): MinecraftNameChangedTimeRequestModel {
            return HttpClientWrapper.create(HttpClientWrapper.Method.GET)
                .uri(apiAccessor.minecraftNameChangeStateUrl)
                .header(tokenHeader())
                .timeout(5000)
                .reqTimeout(5000)
                .retry(5)
                .sendAndReadJson(MinecraftNameChangedTimeRequestModel::class.java)
        }

        override fun getTaskName(): Text {
            return translatable("core.oauth.task.changedTime.text")
        }
    }

    inner class EnableAccountCapeTask(private val index: String? = null) : AbstractAction() {
        @Throws(Exception::class)
        override fun execute() {
            profile = HttpClientWrapper.create(HttpClientWrapper.Method.PUT)
                .uri(apiAccessor.minecraftCapeModifyUrl)
                .header(tokenHeader())
                .entityJson(
                    MinecraftEnableCapeResponseModel(index)
                )
                .timeout(5000)
                .reqTimeout(5000)
                .retry(5)
                .sendAndReadJson(MinecraftProfileRequestModel::class.java)
        }

        override fun getTaskName(): Text {
            return translatable("core.oauth.task.enable_cape.text")
        }
    }

    inner class ResetSkinTask : AbstractAction() {
        @Throws(Exception::class)
        override fun execute() {
            profile = HttpClientWrapper.create(HttpClientWrapper.Method.DELETE)
                .uri(apiAccessor.minecraftSkinResetUrl)
                .header(tokenHeader())
                .timeout(5000)
                .reqTimeout(5000)
                .retry(5)
                .sendAndReadJson(MinecraftProfileRequestModel::class.java)
        }

        override fun getTaskName(): Text {
            return translatable("core.oauth.task.reset_skin.text")
        }
    }

    inner class UploadSkinTask(
        private val file: File? = null,
        private val isSlim: Boolean = false
    ) : AbstractAction() {
        @Throws(Exception::class)
        override fun execute() {
            if (!ImageUtil.isValidImage(file)) throw IOException("bad image")
            profile = HttpClientWrapper.create(HttpClientWrapper.Method.POST)
                .uri(apiAccessor.minecraftSkinModifyUrl)
                .header(tokenHeader())
                .entity(
                    MultipartEntityBuilder.create()
                        .addBinaryBody("file", file)
                        .addTextBody("variant", if (isSlim) "slim" else "classic")
                        .build()
                )
                .timeout(5000)
                .reqTimeout(5000)
                .retry(5)
                .sendAndReadJson(MinecraftProfileRequestModel::class.java)
        }

        override fun getTaskName(): Text {
            return translatable("core.oauth.task.upload_skin.text")
        }
    }

    companion object {
        @JvmStatic
        private val NAME_PATTERN: Pattern = Pattern.compile("([^a-zA-Z0-9_].*)")

        @JvmStatic
        private var apiAccessor: Accessor = Accessor.INSTANCE

        @JvmStatic
        fun setApiAccessor(apiAccessor: Accessor) {
            if (Companion.apiAccessor === Accessor.INSTANCE) Companion.apiAccessor = apiAccessor
        }

        @JvmStatic
        fun create(minecraftUser: MinecraftRequestModel, deviceCode: DeviceCodeConverterModel): MicrosoftAccount {
            return MicrosoftAccount(minecraftUser, (deviceCode.model!!.refreshToken)!!)
        }

        @JvmStatic
        fun create(accessToken: String, refreshToken: String, tokenType: String): MicrosoftAccount {
            return MicrosoftAccount(accessToken, refreshToken, tokenType)
        }
    }
}

