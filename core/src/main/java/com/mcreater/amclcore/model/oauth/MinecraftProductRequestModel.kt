package com.mcreater.amclcore.model.oauth

import com.mcreater.amclcore.annotations.OAuthLoginModel
import com.mcreater.amclcore.annotations.RequestModel

@RequestModel
@OAuthLoginModel
data class MinecraftProductRequestModel(
    var items: List<MinecraftProductItemModel?>? = null,
    var signature: String? = null,
    var keyId: String? = null
) {
    @RequestModel
    @OAuthLoginModel
    data class MinecraftProductItemModel(
        var name: String? = null,
        var signature: String? = null
    )
}
