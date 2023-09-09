package com.mcreater.amclcore.model.installation.launchermeta;

import com.mcreater.amclcore.util.date.GMTDate;
import com.mcreater.amclcore.util.hash.Sha1String;
import com.mcreater.amclcore.util.url.MinecraftMirroredResourceURL;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LauncherMetaVersionModel {
    private String id;
    private String type;
    private MinecraftMirroredResourceURL url;
    private GMTDate time;
    private GMTDate releaseTime;
    private Sha1String sha1;
    private int complianceLevel;
}
