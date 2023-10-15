package com.mcreater.amclcore.model.oauth.session

import com.mcreater.amclcore.annotations.RequestModel
import java.util.*

@RequestModel
data class MinecraftProfileRequestModel(
    var id: UUID? = null,
    var name: String? = null,
    var skins: List<MinecraftProfileSkinModel>? = null,
    var capes: List<MinecraftProfileCapeModel>? = null,
    var profileActions: Map<Any?, Any?>? = null,
    var path: String? = null,
    var errorType: String? = null,
    var error: String? = null,
    var details: MinecraftNameChangeableRequestModel? = null,
    var errorMessage: String? = null,
    var developerMessage: String? = null
) {
    @RequestModel
    data class MinecraftProfileSkinModel(
        var id: UUID? = null,
        var state: State? = null,
        var url: String? = null,
        var variant: Variant? = null
    )


    @RequestModel
    data class MinecraftProfileCapeModel(
        var id: UUID? = null,
        var state: State? = null,
        var url: String? = null,
        var alias: String? = null
    ) {
        fun createId(): String {
            return id.toString()
        }
    }


    enum class State {
        ACTIVE,
        INACTIVE;

        companion object {
            @JvmStatic
            fun parse(s: String?): State {
                return try {
                    valueOf(s!!)
                } catch (e: Exception) {
                    INACTIVE
                }
            }
        }
    }


    enum class Variant {
        CLASSIC,
        SLIM;

        companion object {
            @JvmStatic
            fun parse(s: String?): Variant {
                return try {
                    valueOf(s!!)
                } catch (e: Exception) {
                    CLASSIC
                }
            }
        }
    }

}