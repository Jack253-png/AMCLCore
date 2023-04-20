package com.mcreater.amclcore;

public class MetaData {
    /**
     * AMCL/AMCLCore azure application id
     * AMCL/AMCLCore azure 应用ID
     */
    public static final String oauthDefaultClientId = "1a969022-f24f-4492-a91c-6f4a6fcb373c";
    /**
     * client id override, using command line {@code -Damclcore.oauth.clientid.override=YOUR_CLIENTID}
     * 客户端ID 覆盖, 使用命令行 {@code -Damclcore.oauth.clientid.override=你的客户端ID}
     */
    public static final String oauthClientIdOverridePropertyName = "amclcore.oauth.clientid.override";

    private MetaData() {
    }
}
