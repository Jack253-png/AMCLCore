package com.mcreater.amclcore.account

import com.mcreater.amclcore.command.CommandArg
import com.mcreater.amclcore.concurrent.task.AbstractAction
import com.mcreater.amclcore.concurrent.task.AbstractTask
import com.mcreater.amclcore.model.oauth.session.MinecraftNameChangedTimeRequestModel
import com.mcreater.amclcore.util.StringUtil
import java.io.File
import java.util.*


abstract class AbstractAccount {
    enum class UserType {
        MOJANG,
        OFFLINE
    }

    open var accountName: String? = null
    open var uuid: UUID? = null
    var accessToken: String? = null

    constructor(accountName: String, uuid: UUID, accessToken: String) {
        this.accountName = accountName
        this.uuid = uuid
        this.accessToken = accessToken
    }

    constructor(accessToken: String) {
        this.accessToken = accessToken
    }

    constructor(accountName: String, uuid: UUID) {
        this.accountName = accountName
        this.uuid = uuid
    }

    /**
     * create account refresh task<br></br>
     * 创建账户刷新任务
     *
     * @return the created task<br></br>被创建的任务
     */
    abstract fun refreshAsync(): AbstractAction?

    /**
     * create fetch profile task<br></br>
     * 创建档案获取任务
     *
     * @return the created task<br></br>被创建的任务
     */
    abstract fun fetchProfileAsync(): AbstractAction?

    /**
     * create check profile's AccessToken is outdated or not task<br></br>
     * 创建检查账户 AccessToken 是否失效与否的任务
     *
     * @return the created task<br></br>被创建的任务
     */
    abstract fun validateAccountAsync(): AbstractTask<Boolean?>?

    /**
     * create disable cape task<br></br>
     * 创建禁用披风任务
     *
     * @return the created task<br></br>被创建的任务
     */
    abstract fun disableAccountCapeAsync(): AbstractAction?

    /**
     * create enable selected cape task<br></br>
     * 创建启用选定披风任务
     *
     * @param id the cape index<br></br>披风序列号
     * @return the created task<br></br>被创建的任务
     */
    abstract fun enableAccountCapeAsync(id: String?): AbstractAction?

    /**
     * create check account name changeable task<br></br>
     * 创建检查账户名是否可以改变的任务
     *
     * @return the created task<br></br>被创建的任务
     */
    abstract fun checkAccountNameChangeableAsync(newName: String): AbstractTask<Boolean?>?

    /**
     * create change account name task<br></br>
     * 创建更改账户名任务
     *
     * @return the created task<br></br>被创建的任务
     */
    abstract fun changeAccountNameAsync(newName: String): AbstractAction?

    /**
     * create check account name changed time task<br></br>
     * 创建检查账户名改变时间的任务
     *
     * @return the created task<br></br>被创建的任务
     */
    abstract fun checkAccountNameChangedTimeAsync(): AbstractTask<MinecraftNameChangedTimeRequestModel?>?

    /**
     * check account name is allowed<br></br>
     * 检查账户名是否允许
     *
     * @return the check result<br></br>检查结果
     */
    abstract fun accountNameAllowed(name: String?): Boolean

    /**
     * create reset skin task<br></br>
     * 创建重置皮肤任务
     *
     * @return the created task<br></br>被创建的任务
     */
    abstract fun resetSkinAsync(): AbstractAction?

    /**
     * create upload skin task<br></br>
     * 创建上传皮肤任务
     *
     * @return the created task<br></br>被创建的任务
     */
    abstract fun uploadSkinAsync(file: File?, isSlim: Boolean): AbstractAction?
    fun toMicrosoftAccount(): MicrosoftAccount {
        return this as MicrosoftAccount
    }

    fun toOffLineAccount(): OfflineAccount {
        return this as OfflineAccount
    }

    val isMicrosoftAccount: Boolean
        get() = this is MicrosoftAccount
    val isOffLineAccount: Boolean
        get() = this is OfflineAccount
    abstract val addonArgs: List<CommandArg?>?

    abstract fun preLaunchAsync(): AbstractAction?
    fun toProfile(): Profile {
        return Profile(StringUtil.toNoLineUUID(uuid), accountName)
    }

    data class Profile(
        val id: String? = null,
        val name: String? = null
    )
}
