package com.mcreater.amclcore.model.oauth

import java.util.*

enum class LoginDeviceCodeErrorType {
    AUTHORIZATION_PENDING,
    SLOW_DOWN,
    EXPIRED_TOKEN,
    INVALID_GRANT;

    companion object {
        @JvmStatic
        fun parse(s: String): LoginDeviceCodeErrorType {
            return enumValueOf<LoginDeviceCodeErrorType>(s.uppercase(Locale.getDefault()))
        }
    }
}