package com.mcreater.amclcore.account.auth;

import com.mcreater.amclcore.MetaData;
import com.mcreater.amclcore.util.AbstractHttpServer;
import com.mcreater.amclcore.util.KeyUtils;

import java.security.KeyPair;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.mcreater.amclcore.util.JsonUtil.*;

public class YggdrasilAuthServer extends AbstractHttpServer {
    private final KeyPair keyPair = KeyUtils.generateKey();

    public YggdrasilAuthServer(int port) {
        super(port);
        addRoute(Route.create(Pattern.compile("^/$")), this::root);
    }

    private Response root(Map.Entry<IHTTPSession, Matcher> entry) {
        if (keyPair != null) {
            return ok(map(
                    pair("signaturePublickey", KeyUtils.toPEMPublicKey(keyPair.getPublic())),
                    pair("skinDomains", createList("127.0.0.1", "localhost")),
                    pair("meta", map(
                            pair("serverName", MetaData.getLauncherName()),
                            pair("implementationName", MetaData.getLauncherName()),
                            pair("implementationVersion", MetaData.getLauncherFullVersion()),
                            pair("feature.non_email_login", true)
                    ))
            ));
        } else {
            return ok(map(
                    pair("skinDomains", createList("127.0.0.1", "localhost")),
                    pair("meta", map(
                            pair("serverName", MetaData.getLauncherName()),
                            pair("implementationName", MetaData.getLauncherName()),
                            pair("implementationVersion", MetaData.getLauncherFullVersion()),
                            pair("feature.non_email_login", true)
                    ))
            ));
        }
    }
}
