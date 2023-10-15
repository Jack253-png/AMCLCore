package com.mcreater.amclcore.model.oauth

import com.mcreater.amclcore.annotations.OAuthLoginModel

@OAuthLoginModel
data class XBLAccountModel(
    var token: String? = null,
    var hash: String? = null
)