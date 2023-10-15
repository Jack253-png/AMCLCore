package com.mcreater.amclcore.model.oauth.session

import com.mcreater.amclcore.annotations.RequestModel

@RequestModel
data class MinecraftNameChangeableRequestModel(
    var status: State? = null
) {
    enum class State {
        DUPLICATE,
        AVAILABLE,
        NOT_ALLOWED;

        companion object {
            @JvmStatic
            fun parse(s: String?): State {
                return try {
                    valueOf(s!!)
                } catch (e: Exception) {
                    DUPLICATE
                }
            }
        }
    }

}
