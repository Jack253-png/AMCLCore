package com.mcreater.amclcore

import com.mcreater.amclcore.util.PropertyUtil
import kotlin.math.max

class MetaData {
    enum class Channel(val realname: String) {
        RELEASE(""),
        CANDIDATE("rc"),
        BETA("beta"),
        ALPHA("alpha")
    }

    companion object {
        /**
         * AMCL/AMCLCore azure application id<br></br>
         * AMCL/AMCLCore azure 应用ID
         */
        const val oauthDefaultClientId = "1a969022-f24f-4492-a91c-6f4a6fcb373c"

        /**
         * client id override, using command line -Damclcore.oauth.clientid.override=YOUR_CLIENTID<br></br>
         * 客户端ID 覆盖, 使用命令行 -Damclcore.oauth.clientid.override=你的客户端ID
         */
        const val oauthClientIdOverridePropertyName = "amclcore.oauth.clientid.override"

        @JvmStatic
        fun getOauthDefaultClientId(): String {
            return PropertyUtil.readProperty(oauthClientIdOverridePropertyName, oauthDefaultClientId)
        }

        /**
         * Launcher name<br></br>
         * 启动器名字
         */
        const val launcherName = "AMCLCore"

        /**
         * Launcher version<br></br>
         * 启动器版本
         */
        const val launcherVersion = "0.1"

        /**
         * Launcher version channel<br></br>
         * 启动器版本类型
         */
        val launcherBuildChannelRaw = Channel.ALPHA

        /**
         * Launcher internal version (>= 1)
         * 启动器内部版本 (>= 1)
         */
        const val launcherBuildInternalVersion = 2

        /**
         * launcher name override, using command line -Damclcore.name.override=YOUR_LAUNCHERNAME<br></br>
         * 启动器名称覆盖，使用命令行 -Damclcore.name.override=你的启动器名称
         */
        const val launcherNameOverridePropertyName = "amclcore.name.override"

        @JvmStatic
        fun getLauncherName(): String {
            return PropertyUtil.readProperty(launcherNameOverridePropertyName, launcherName)
        }

        /**
         * launcher version override, using command line -Damclcore.version.override=YOUR_LAUNCHERVERSION<br></br>
         * 启动器版本覆盖，使用命令行 -Damclcore.version.override=你的启动器版本
         */
        const val launcherVersionOverridePropertyName = "amclcore.version.override"

        @JvmStatic
        fun getLauncherVersion(): String {
            return PropertyUtil.readProperty(launcherVersionOverridePropertyName, launcherVersion)
        }

        /**
         * launcher build channel override, using command line -Damclcore.build.channel=YOUR_LAUNCHERBUILDCHANNEL<br></br>
         * 启动器版本类型覆盖，使用命令行 -Damclcore.build.channel=你的启动器版本类型
         */
        const val launcherBuildChannelOverridePropertyName = "amclcore.version.build.channel.override"

        @JvmStatic
        fun getLauncherBuildChannel(): Channel {
            return PropertyUtil.readPropertyEnum(
                Channel::class.java,
                launcherBuildChannelOverridePropertyName,
                launcherBuildChannelRaw
            )
        }

        /**
         * launcher build interal version override, using command line -Damclcore.build.internal.override=YOUR_LAUNCHERBUILDINTERNALVERSION<br></br>
         * 启动器内部构建版本覆盖，使用命令行 -Damclcore.build.internal.override=你的内部构建版本号
         */
        const val launcherBuildInternalVersionOverridePropertyName = "amclcore.version.build.internal.override"

        @JvmStatic
        fun getLauncherBuildInternalVersion(): Int {
            return max(
                PropertyUtil.readPropertyInteger(
                    launcherBuildInternalVersionOverridePropertyName,
                    launcherBuildInternalVersion
                ).toDouble(), 1.0
            )
                .toInt()
        }

        @JvmStatic
        fun getLauncherFullVersion(): String {
            return if (getLauncherBuildChannel() == Channel.RELEASE) {
                getLauncherVersion()
            } else String.format(
                "%s-%s%d",
                getLauncherVersion(),
                getLauncherBuildChannel().name,
                getLauncherBuildInternalVersion()
            )
        }

        @JvmStatic
        fun getLauncherFullName(): String {
            return java.lang.String.join(" ", getLauncherName(), getLauncherFullVersion())
        }

        /**
         * ansi enable override, using command line -Damclcore.ansi.override=BOOLEAN<br></br>启用ANSI覆盖, 使用命令行 -Damclcore.ansi.override=BOOLEAN
         */
        const val useAnsiOutputOverridePropertyName = "amclcore.ansi.override"

        @JvmStatic
        fun isUseAnsiOutputOverride(): Boolean {
            return PropertyUtil.readPropertyBoolean(useAnsiOutputOverridePropertyName, true)
        }
    }
}