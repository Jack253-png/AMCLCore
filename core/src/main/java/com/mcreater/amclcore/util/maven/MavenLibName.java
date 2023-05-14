package com.mcreater.amclcore.util.maven;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.ToString;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@ToString
public class MavenLibName {
    private String groupId;
    private String artifactId;
    private String version;

    private String platform;

    public String getName() {
        if (platform == null) return String.format("%s:%s:%s", groupId, artifactId, version);
        else return String.format("%s:%s:%s:%s", groupId, artifactId, version, platform);
    }

    public static MavenLibName of(String groupId, String artifactId, String version, String platform) {
        return new MavenLibName(groupId, artifactId, version, platform);
    }

    public static MavenLibName of(String pom) {
        String[] parts = pom.split(":");
        return new MavenLibName(parts[0], parts[1], parts[2], parts.length >= 4 ? parts[3] : null);
    }
}
