package com.mcreater.amclcore.model.oauth;

public enum LoginDeviceCodeErrorType {
    AUTHORIZATION_PENDING,
    SLOW_DOWN,
    EXPIRED_TOKEN,
    INVALID_GRANT;

    public static LoginDeviceCodeErrorType parse(String s) {
        return valueOf(LoginDeviceCodeErrorType.class, s.toUpperCase());
    }
}
