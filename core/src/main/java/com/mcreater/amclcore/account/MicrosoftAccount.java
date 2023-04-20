package com.mcreater.amclcore.account;

public class MicrosoftAccount extends AbstractAccount {
    private MicrosoftAccount(String accessToken) {
        super(accessToken);
    }

    public static MicrosoftAccount create(String accessToken) {
        return new MicrosoftAccount(accessToken);
    }
}
