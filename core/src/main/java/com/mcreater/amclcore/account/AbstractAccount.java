package com.mcreater.amclcore.account;

import lombok.Data;

import java.util.UUID;

@Data
public abstract class AbstractAccount {
    private String userName;
    private UUID uuid;
    private String accessToken;

    public AbstractAccount(String userName, UUID uuid, String accessToken) {
        setUserName(userName);
        setUuid(uuid);
        setAccessToken(accessToken);
    }

    public AbstractAccount(String accessToken) {
        setAccessToken(accessToken);
    }
}
