package com.mcreater.amclcore.util.url;

import com.mcreater.amclcore.util.HttpClientWrapper;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.jetbrains.annotations.NotNull;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class MinecraftMirroredResourceURL {
    private static URL replaceURL(URL base, String old, String rep) {
        try {
            return URI.create(base.toString().replace(old, rep)).toURL();
        } catch (MalformedURLException e) {
            return base;
        }
    }

    private static void addURL(@NotNull Map<String, Function<URL, URL>> map, String old, String rep) {
        map.put(
                old,
                u -> replaceURL(u, old, rep)
        );
    }

    public enum MirrorServer {
        MOJANG(
                new HashMap<>()
        ),
        BMCLAPI(
                new HashMap<String, Function<URL, URL>>() {{
                    addURL(this, "launchermeta.mojang.com", "bmclapi2.bangbang93.com");
                    addURL(this, "launcher.mojang.com", "bmclapi2.bangbang93.com");
                    addURL(this, "piston-meta.mojang.com", "bmclapi2.bangbang93.com");
                    addURL(this, "resources.download.minecraft.net", "bmclapi2.bangbang93.com/assets");
                    addURL(this, "libraries.minecraft.net", "bmclapi2.bangbang93.com/maven");
                    addURL(this, "files.minecraftforge.net/maven", "bmclapi2.bangbang93.com/maven");
                    addURL(this, "maven.minecraftforge.net", "bmclapi2.bangbang93.com/maven");
                    addURL(this, "dl.liteloader.com/versions/versions.json", "bmclapi.bangbang93.com/maven/com/mumfrey/liteloader/versions.json");
                    addURL(this, "repo.mumfrey.com/content/repositories/snapshots", "bmclapi2.bangbang93.com/maven");
                    addURL(this, "dl.liteloader.com/repo", "bmclapi2.bangbang93.com/maven");
                    addURL(this, "authlib-injector.yushi.moe", "bmclapi2.bangbang93.com/mirrors/authlib-injector");
                    addURL(this, "meta.fabricmc.net", "bmclapi2.bangbang93.com/fabric-meta");
                    addURL(this, "maven.fabricmc.net", "bmclapi2.bangbang93.com/maven");
                }}
        ),
        MCBBS(
                new HashMap<String, Function<URL, URL>>() {{
                    addURL(this, "launchermeta.mojang.com", "download.mcbbs.net");
                    addURL(this, "launcher.mojang.com", "download.mcbbs.net");
                    addURL(this, "piston-meta.mojang.com", "download.mcbbs.net");
                    addURL(this, "resources.download.minecraft.net", "download.mcbbs.net/assets");
                    addURL(this, "libraries.minecraft.net", "download.mcbbs.net/maven");
                    addURL(this, "files.minecraftforge.net/maven", "download.mcbbs.net/maven");
                    addURL(this, "maven.minecraftforge.net", "download.mcbbs.net/maven");
                    addURL(this, "dl.liteloader.com/versions/versions.json", "download.mcbbs.net/maven/com/mumfrey/liteloader/versions.json");
                    addURL(this, "repo.mumfrey.com/content/repositories/snapshots", "download.mcbbs.net/maven");
                    addURL(this, "dl.liteloader.com/repo", "download.mcbbs.net/maven");
                    addURL(this, "authlib-injector.yushi.moe", "download.mcbbs.net/mirrors/authlib-injector");
                    addURL(this, "meta.fabricmc.net", "download.mcbbs.net/fabric-meta");
                    addURL(this, "maven.fabricmc.net", "download.mcbbs.net/maven");
                }}
        );
        private final Map<String, Function<URL, URL>> parseMap;

        MirrorServer(@NotNull Map<String, Function<URL, URL>> parseMap) {
            this.parseMap = parseMap;
        }
    }

    @Getter
    private final URL rawUrl;
    @Getter
    @NotNull
    private final MirrorServer server;

    public MinecraftMirroredResourceURL upSource() {
        MirrorServer serverNext;
        switch (server) {
            case MOJANG:
                serverNext = MirrorServer.BMCLAPI;
                break;
            default:
            case BMCLAPI:
            case MCBBS:
                serverNext = MirrorServer.MCBBS;
                break;
        }
        return toSource(serverNext);
    }

    public MinecraftMirroredResourceURL downSource() {
        MirrorServer serverNext;
        switch (server) {
            default:
            case BMCLAPI:
            case MOJANG:
                serverNext = MirrorServer.MOJANG;
                break;
            case MCBBS:
                serverNext = MirrorServer.BMCLAPI;
                break;
        }
        return toSource(serverNext);
    }

    public MinecraftMirroredResourceURL toSource(@NotNull MirrorServer server) {
        return create(rawUrl, server);
    }

    public static MinecraftMirroredResourceURL create(URL url) {
        return create(url, MirrorServer.MOJANG);
    }

    public static MinecraftMirroredResourceURL create(URL url, @NotNull MirrorServer server) {
        return new MinecraftMirroredResourceURL(url, server);
    }

    public static MinecraftMirroredResourceURL create(String url) throws MalformedURLException {
        return create(URI.create(url).toURL());
    }

    public static MinecraftMirroredResourceURL create(String url, @NotNull MirrorServer server) throws MalformedURLException {
        return create(URI.create(url).toURL(), server);
    }

    public ImmutablePair<HttpClientWrapper.Scheme, String> toDownloadFormat() {
        return ImmutablePair.of(
                HttpClientWrapper.Scheme.valueOf(rawUrl.getProtocol().toUpperCase()),
                rawUrl.getHost()
                        + (rawUrl.getPort() == -1 ? "" : ":" + rawUrl.getPort())
                        + rawUrl.getPath()
                        + (rawUrl.getQuery() == null ? "" : "?" + rawUrl.getQuery())
        );
    }

    public ImmutablePair<HttpClientWrapper.Scheme, String> toDownloadFormatWithMirror() {
        AtomicReference<URL> resultURL = new AtomicReference<>();
        server.parseMap.forEach((s, urlurlFunction) -> {
            if (resultURL.get() != null) return;
            if (rawUrl.toString().contains(s)) resultURL.set(urlurlFunction.apply(rawUrl));
        });
        if (resultURL.get() == null) resultURL.set(rawUrl);
        return ImmutablePair.of(
                HttpClientWrapper.Scheme.valueOf(resultURL.get().getProtocol().toUpperCase()),
                resultURL.get().getHost()
                        + (resultURL.get().getPort() == -1 ? "" : ":" + resultURL.get().getPort())
                        + resultURL.get().getPath()
                        + (resultURL.get().getQuery() == null ? "" : "?" + resultURL.get().getQuery())
        );
    }
}
