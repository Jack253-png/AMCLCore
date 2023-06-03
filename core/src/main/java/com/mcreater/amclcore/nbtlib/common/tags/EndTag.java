package com.mcreater.amclcore.nbtlib.common.tags;

import org.jetbrains.annotations.NotNull;

public final class EndTag extends AbstractTag<Void> implements Comparable<EndTag> {
    public static final EndTag INSTANCE = new EndTag();

    private EndTag() {
        super(null);
    }

    public EndTag clone() {
        return INSTANCE;
    }

    public int compareTo(@NotNull EndTag o) {
        return 0;
    }
}
