package com.mcreater.amclcore;

import lombok.Getter;

public class MetaData {
    /**
     * AMCL/AMCLCore azure application id
     */
    @Getter
    public static final String oauthDefaultClientId = "1a969022-f24f-4492-a91c-6f4a6fcb373c";
    /**
     * client id override, using command line {@code -Damclcore.oauth.clientid.override=YOUR_CLIENTID}
     */
    @Getter
    public static final String oauthClientIdOverridePropertyName = "amclcore.oauth.clientid.override";
}
