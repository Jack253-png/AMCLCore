package com.mcreater.amclcore.account;

import com.mcreater.amclcore.concurrent.task.AbstractAction;
import com.mcreater.amclcore.concurrent.task.AbstractTask;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

import static java.util.Objects.requireNonNull;

public abstract class AbstractAccount {
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

    public AbstractAccount(String accountName, UUID uuid, String accessToken) {
        setAccountName(accountName);
        setUuid(uuid);
        setAccessToken(accessToken);
    }

    public AbstractAccount(String accessToken) {
        setAccessToken(accessToken);
    }

    /**
     * create account refresh task<br>
     * 创建账户刷新任务
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
}
