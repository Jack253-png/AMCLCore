package com.mcreater.amclcore.account.auth;

import com.mcreater.amclcore.MetaData;
import com.mcreater.amclcore.account.AbstractAccount;
import com.mcreater.amclcore.account.OfflineAccount;
import com.mcreater.amclcore.util.AbstractHttpServer;
import com.mcreater.amclcore.util.KeyUtils;
import com.mcreater.amclcore.util.NetworkUtils;
import com.mcreater.amclcore.util.StringUtil;
import lombok.Getter;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.KeyPair;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.mcreater.amclcore.util.AbstractHttpServer.Route.IS_GET;
import static com.mcreater.amclcore.util.AbstractHttpServer.Route.IS_POST;
import static com.mcreater.amclcore.util.IOStreamUtil.read;
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
        addRoute(Route.create(Pattern.compile("/sessionserver/session/minecraft/hasJoined"), IS_GET), this::hasJoined);
        addRoute(Route.create(Pattern.compile("/sessionserver/session/minecraft/join"), IS_GET), this::joinServer);
        addRoute(Route.create(Pattern.compile("/sessionserver/session/minecraft/profile/(?<uuid>[a-f0-9]{32})"), IS_GET), this::profile);
        addRoute(Route.create(Pattern.compile("/textures/(?<hash>[a-f0-9]{64})"), IS_GET), this::texture);
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

    public Optional<OfflineAccount> findByUUID(String uuid) {
        return accounts.stream().filter(a -> StringUtil.toNoLineUUID(a.getUuid()).equals(uuid)).findFirst();
    }

    private Response hasJoined(Map.Entry<IHTTPSession, Matcher> entry) {
        Map<String, String> inp = map(NetworkUtils.parseQuery(entry.getKey().getQueryParameterString()));
        if (!inp.containsKey("username")) return badRequest();

        Optional<OfflineAccount> offlineAccount = findByName(inp.get("username"));
        if (offlineAccount.isPresent()) return ok(offlineAccount.get().toSkinResponse(getHost(), keyPair.getPrivate()));
        else return badRequest();
    }

    private Response profile(Map.Entry<IHTTPSession, Matcher> entry) {
        String uuid = entry.getValue().group("uuid");
        Optional<OfflineAccount> account = findByUUID(uuid);

        if (account.isPresent()) return ok(account.get().toSkinResponse(getHost(), keyPair.getPrivate()));
        else return noContent();
    }

    private Response joinServer(Map.Entry<IHTTPSession, Matcher> entry) {
        return noContent();
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

    private Response texture(Map.Entry<IHTTPSession, Matcher> entry) throws IOException {
        String hash = entry.getValue().group("hash");
        if (OfflineAccount.Texture.hasTexture(hash)) {
            OfflineAccount.Texture texture = OfflineAccount.Texture.getTexture(hash);
            byte[] data = read(texture.getSource());
            Response response = newFixedLengthResponse(Response.Status.OK, "image/png", new ByteArrayInputStream(data), data.length);
            response.addHeader("Etag", String.format("\"%s\"", hash));
            response.addHeader("Cache-Control", "max-age=2592000, public");
            return response;
        } else return notFound();
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
