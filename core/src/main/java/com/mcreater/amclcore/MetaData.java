package com.mcreater.amclcore;

import com.mcreater.amclcore.util.PropertyUtil;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MetaData {
    public enum Channel {
        RELEASE(""),
        CANDIDATE("rc"),
        BETA("beta"),
        ALPHA("alpha");
        public final String name;

        Channel(String name) {
            this.name = name;
        }
    }

    /**
     * AMCL/AMCLCore azure application id<br>
     * AMCL/AMCLCore azure 应用ID
     */
    public static final String oauthDefaultClientId = "1a969022-f24f-4492-a91c-6f4a6fcb373c";
    /**
     * client id override, using command line {@code -Damclcore.oauth.clientid.override=YOUR_CLIENTID}<br>
     * 客户端ID 覆盖, 使用命令行 {@code -Damclcore.oauth.clientid.override=你的客户端ID}
     */
    public static final String oauthClientIdOverridePropertyName = "amclcore.oauth.clientid.override";

    public static String getOauthDefaultClientId() {
        return PropertyUtil.readProperty(oauthClientIdOverridePropertyName, oauthDefaultClientId);
    }

    /**
     * Launcher name<br>
     * 启动器名字
     */
    public static final String launcherName = "AMCLCore";
    /**
     * Launcher version<br>
     * 启动器版本
     */
    public static final String launcherVersion = "0.1";
    /**
     * Launcher version channel<br>
     * 启动器版本类型
     */
    public static final Channel launcherBuildChannel = Channel.ALPHA;
    /**
     * Launcher internal version (>= 1)
     * 启动器内部版本 (>= 1)
     */
    public static final int launcherBuildInternalVersion = 1;
    /**
     * launcher name override, using command line {@code -Damclcore.name.override=YOUR_LAUNCHERNAME}<br>
     * 启动器名称覆盖，使用命令行 {@code -Damclcore.name.override=你的启动器名称}
     */
    public static final String launcherNameOverridePropertyName = "amclcore.name.override";

    public static String getLauncherName() {
        return PropertyUtil.readProperty(launcherNameOverridePropertyName, launcherName);
    }

    /**
     * launcher version override, using command line {@code -Damclcore.version.override=YOUR_LAUNCHERVERSION}<br>
     * 启动器版本覆盖，使用命令行 {@code -Damclcore.version.override=你的启动器版本}
     */
    public static final String launcherVersionOverridePropertyName = "amclcore.version.override";

    public static String getLauncherVersion() {
        return PropertyUtil.readProperty(launcherVersionOverridePropertyName, launcherVersion);
    }

    /**
     * launcher build channel override, using command line {@code -Damclcore.build.channel=YOUR_LAUNCHERBUILDCHANNEL}<br>
     * 启动器版本类型覆盖，使用命令行 {@code -Damclcore.build.channel=你的启动器版本类型}
     */
    public static final String launcherBuildChannelOverridePropertyName = "amclcore.version.build.channel.override";

    public static Channel getLauncherBuildChannel() {
        return PropertyUtil.readPropertyEnum(Channel.class, launcherBuildChannelOverridePropertyName, launcherBuildChannel);
    }

    /**
     * launcher build interal version override, using command line {@code -Damclcore.build.internal.override=YOUR_LAUNCHERBUILDINTERNALVERSION}<br>
     * 启动器内部构建版本覆盖，使用命令行 {@code -Damclcore.build.internal.override=你的内部构建版本号}
     */
    public static final String launcherBuildInternalVersionOverridePropertyName = "amclcore.version.build.internal.override";

    public static int getLauncherBuildInternalVersion() {
        return Math.max(PropertyUtil.readPropertyInteger(launcherBuildInternalVersionOverridePropertyName, 1), 1);
    }

    public static String getLauncherFullVersion() {
        if (getLauncherBuildChannel() == Channel.RELEASE) {
            return getLauncherVersion();
        }
        return String.format("%s-%s%d", getLauncherVersion(), getLauncherBuildChannel().name, getLauncherBuildInternalVersion());
    }
}
