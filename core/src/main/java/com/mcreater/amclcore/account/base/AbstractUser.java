package com.mcreater.amclcore.account.base;

import lombok.Data;

import java.util.UUID;

@Data
public abstract class AbstractUser {
    private String userName;
    private UUID uuid;
    private String accessToken;
    public AbstractUser(String userName, UUID uuid, String accessToken) {
        setUserName(userName);
        setUuid(uuid);
        setAccessToken(accessToken);
    }
}
