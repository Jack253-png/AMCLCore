package com.mcreater.amclcore.account;

import com.mcreater.amclcore.concurrent.task.AbstractAction;
import com.mcreater.amclcore.concurrent.task.AbstractTask;
import com.mcreater.amclcore.model.oauth.session.MinecraftNameChangedTimeRequestModel;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.UUID;

import static java.util.Objects.requireNonNull;

public abstract class AbstractAccount {
    public enum UserType {
        MOJANG,
        OFFLINE
    }

    @Getter
    private String accountName;
    @Getter
    private UUID uuid;
    @Getter
    private String accessToken;

    protected void setAccountName(@NotNull String accountName) {
        this.accountName = requireNonNull(accountName);
    }

    protected void setUuid(@NotNull UUID uuid) {
        this.uuid = requireNonNull(uuid);
    }

    protected void setAccessToken(@NotNull String accessToken) {
        this.accessToken = requireNonNull(accessToken);
    }

    public AbstractAccount(@NotNull String accountName, @NotNull UUID uuid, @NotNull String accessToken) {
        setAccountName(accountName);
        setUuid(uuid);
        setAccessToken(accessToken);
    }

    public AbstractAccount(@NotNull String accessToken) {
        setAccessToken(accessToken);
    }

    public AbstractAccount(@NotNull String accountName, @NotNull UUID uuid) {
        setAccountName(accountName);
        setUuid(uuid);
    }

    /**
     * create account refresh task<br>
     * 创建账户刷新任务
     *
     * @return the created task<br>被创建的任务
     */
    public abstract AbstractAction refreshAsync();

    /**
     * create fetch profile task<br>
     * 创建档案获取任务
     *
     * @return the created task<br>被创建的任务
     */
    public abstract AbstractAction fetchProfileAsync();

    /**
     * create check profile's AccessToken is outdated or not task<br>
     * 创建检查账户 AccessToken 是否失效与否的任务
     *
     * @return the created task<br>被创建的任务
     */
    public abstract AbstractTask<Boolean> validateAccountAsync();

    /**
     * create disable cape task<br>
     * 创建禁用披风任务
     *
     * @return the created task<br>被创建的任务
     */
    public abstract AbstractAction disableAccountCapeAsync();

    /**
     * create enable selected cape task<br>
     * 创建启用选定披风任务
     *
     * @param id the cape index<br>披风序列号
     * @return the created task<br>被创建的任务
     */
    public abstract AbstractAction enableAccountCapeAsync(String id);

    /**
     * create check account name changeable task<br>
     * 创建检查账户名是否可以改变的任务
     *
     * @return the created task<br>被创建的任务
     */
    public abstract AbstractTask<Boolean> checkAccountNameChangeableAsync(@NotNull String newName);

    /**
     * create change account name task<br>
     * 创建更改账户名任务
     *
     * @return the created task<br>被创建的任务
     */
    public abstract AbstractAction changeAccountNameAsync(@NotNull String newName);

    /**
     * create check account name changed time task<br>
     * 创建检查账户名改变时间的任务
     *
     * @return the created task<br>被创建的任务
     */
    public abstract AbstractTask<MinecraftNameChangedTimeRequestModel> checkAccountNameChangedTimeAsync();

    /**
     * check account name is allowed<br>
     * 检查账户名是否允许
     *
     * @return the check result<br>检查结果
     */
    public abstract boolean accountNameAllowed(String name);

    /**
     * create reset skin task<br>
     * 创建重置皮肤任务
     *
     * @return the created task<br>被创建的任务
     */
    public abstract AbstractAction resetSkinAsync();

    /**
     * create upload skin task<br>
     * 创建上传皮肤任务
     *
     * @return the created task<br>被创建的任务
     */
    public abstract AbstractAction uploadSkinAsync(File file, boolean isSlim);

    public final MicrosoftAccount toMicrosoftAccount() {
        return (MicrosoftAccount) this;
    }

    public final OfflineAccount toOffLineAccount() {
        return (OfflineAccount) this;
    }

    public final boolean isMicrosoftAccount() {
        return this instanceof MicrosoftAccount;
    }

    public final boolean isOffLineAccount() {
        return this instanceof OfflineAccount;
    }
}
