package com.mcreater.amclcore.util.maven;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.mcreater.amclcore.util.JsonUtil.createList;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@ToString
@Getter
public class MavenLibName {
    private String groupId;
    private String artifactId;
    private String version;

    private String platform;

    public String getName() {
        if (platform == null) return String.format("%s:%s:%s", groupId, artifactId, version);
        else return String.format("%s:%s:%s:%s", groupId, artifactId, version, platform);
    }

    public Path toPath() {
        List<String> names = Arrays.stream(groupId.split("\\.")).collect(Collectors.toList());
        names.addAll(createList(artifactId, version, String.format(
                "%s-%s%s.jar",
                artifactId,
                version,
                Optional.ofNullable(platform).map(s -> "-" + s).orElse(""))
        ));

        return Paths.get("", names.toArray(new String[0]));
    }

    public static MavenLibName of(String groupId, String artifactId, String version, String platform) {
        return new MavenLibName(groupId, artifactId, version, platform);
    }

    public static MavenLibName of(String pom) {
        String[] parts = pom.split(":");
        return new MavenLibName(parts[0], parts[1], parts[2], parts.length >= 4 ? parts[3] : null);
    }
}
