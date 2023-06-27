package com.mcreater.amclcore.account.auth;

import com.mcreater.amclcore.MetaData;
import com.mcreater.amclcore.account.AbstractAccount;
import com.mcreater.amclcore.account.OfflineAccount;
import com.mcreater.amclcore.util.AbstractHttpServer;
import com.mcreater.amclcore.util.KeyUtils;
import lombok.Getter;

import java.security.KeyPair;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.mcreater.amclcore.util.JsonUtil.*;

public class YggdrasilAuthServer extends AbstractHttpServer {
    private final KeyPair keyPair = KeyUtils.generateKey();
    @Getter
    private final List<OfflineAccount> accounts = new Vector<>();

    public YggdrasilAuthServer(int port) {
        super(port);
        addRoute(Route.create(Pattern.compile("^/$")), this::root);
        addRoute(Route.create(Pattern.compile("/status")), this::status);
    }

    public Optional<OfflineAccount> findAccount(UUID uuid) {
        return accounts.stream().filter(a -> a.getUuid().equals(uuid)).findFirst();
    }

    public Optional<UUID> findUUID(OfflineAccount account) {
        return accounts.stream().filter(a -> a == account).findFirst().map(AbstractAccount::getUuid);
    }

    private Response status(Map.Entry<IHTTPSession, Matcher> entry) {
        return ok(map(
                pair("user.count", accounts.size()),
                pair("token.count", 0),
                pair("pendingAuthentication.count", 0)
        ));
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
