package com.mcreater.amclcore.model.oauth

import com.mcreater.amclcore.annotations.OAuthLoginModel

@OAuthLoginModel
data class MinecraftResponseModel(
    var identityToken: String? = null
)
