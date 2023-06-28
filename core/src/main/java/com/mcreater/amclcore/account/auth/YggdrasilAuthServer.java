package com.mcreater.amclcore.account.auth;

import com.mcreater.amclcore.MetaData;
import com.mcreater.amclcore.account.AbstractAccount;
import com.mcreater.amclcore.account.OfflineAccount;
import com.mcreater.amclcore.util.AbstractHttpServer;
import com.mcreater.amclcore.util.KeyUtils;
import com.mcreater.amclcore.util.NetworkUtils;
import lombok.Getter;

import java.security.KeyPair;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.mcreater.amclcore.util.AbstractHttpServer.Route.IS_GET;
import static com.mcreater.amclcore.util.AbstractHttpServer.Route.IS_POST;
import static com.mcreater.amclcore.util.IOStreamUtil.readStream;
import static com.mcreater.amclcore.util.JsonUtil.*;

public class YggdrasilAuthServer extends AbstractHttpServer {
    private final KeyPair keyPair = KeyUtils.generateKey();
    @Getter
    private final List<OfflineAccount> accounts = new Vector<>();

    public YggdrasilAuthServer(int port) {
        super(port);
        addRoute(Route.create(Pattern.compile("^/$"), IS_GET), this::root);
        addRoute(Route.create(Pattern.compile("/status"), IS_GET), this::status);
        addRoute(Route.create(Pattern.compile("/api/profiles/minecraft"), IS_POST), this::profiles);
    }

    public Optional<OfflineAccount> findAccount(UUID uuid) {
        return accounts.stream().filter(a -> a.getUuid().equals(uuid)).findFirst();
    }

    public Optional<UUID> findUUID(OfflineAccount account) {
        return accounts.stream().filter(a -> a == account).findFirst().map(AbstractAccount::getUuid);
    }

    public Optional<OfflineAccount> findByName(String name) {
        return accounts.stream().filter(a -> a.getAccountName().equals(name)).findFirst();
    }

    private Response getIsJoined(Map.Entry<IHTTPSession, Matcher> entry) {
        Map<String, String> inp = map(NetworkUtils.parseQuery(entry.getKey().getQueryParameterString()));
        if (!inp.containsKey("username")) return badRequest();

        Optional<OfflineAccount> offlineAccount = findByName(inp.get("username"));
        if (offlineAccount.isPresent()) {
            return badRequest();
        } else return badRequest();
    }

    private Response profiles(Map.Entry<IHTTPSession, Matcher> entry) {
        List<String> names = GSON_PARSER.fromJson(readStream(entry.getKey().getInputStream()), List.class);

        return ok(
                accounts.stream()
                        .filter(p -> names.contains(p.getAccountName()))
                        .map(OfflineAccount::toProfile)
                        .collect(Collectors.toList())
        );
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
