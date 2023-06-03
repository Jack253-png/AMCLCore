package com.mcreater.amclcore.nbtlib.common.tags;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class NamedTag {
    public String name;
    private AbstractTag<?> tag;
}
