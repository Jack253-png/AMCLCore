package com.mcreater.amclcore.account;

import com.mcreater.amclcore.concurrent.AbstractAction;
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

    public abstract AbstractAction refreshAsync();

    public abstract AbstractAction fetchProfileAsync();
}
