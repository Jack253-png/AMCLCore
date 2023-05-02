package com.mcreater.amclcore.util.url;

import com.mcreater.amclcore.util.HttpClientWrapper;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.jetbrains.annotations.NotNull;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class MinecraftMirroredResourceURL {
    public enum MirrorServer {
        MOJANG,
        BMCLAPI,
        MCBBS
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

    public ImmutableTriple<HttpClientWrapper.Method, String, String> toDownloadFormat() {
        return ImmutableTriple.of(
                HttpClientWrapper.Method.valueOf(rawUrl.getProtocol().toUpperCase()),
                rawUrl.getHost(),
                rawUrl.getPath() + "?" + rawUrl.getQuery()
        );
    }
}
