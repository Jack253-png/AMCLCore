package com.mcreater.amclcore.util.sets;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@AllArgsConstructor
@Builder
@Getter
public class ImmutableDoubleValueSet<V1, V2> {
    private final V1 value1;
    private final V2 value2;
}
