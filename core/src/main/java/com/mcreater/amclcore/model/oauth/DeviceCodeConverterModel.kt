package com.mcreater.amclcore.model.oauth

data class DeviceCodeConverterModel(
    var model: TokenResponseModel? = null,
    var isDevice: Boolean = false
) {
    fun createAccessToken(): String {
        return if (isDevice) "d=" + (model?.accessToken ?: "") else (model?.accessToken ?: "")
    }
}
