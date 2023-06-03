package com.mcreater.amclcore.java;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class MemorySize {
    public enum MemoryUnit {
        BYTES("b"),
        KILOBYTES("k"),
        MEGABYTES("m"),
        GIGABYTES("g");
        @Getter
        private final String name;

        MemoryUnit(String name) {
            this.name = name;
        }
    }

    private long mem;

    public static MemorySize create(long mem) {
        return new MemorySize(mem);
    }

    public static MemorySize create(long mem, MemoryUnit type) {
        return create(mem + type.getName());
    }

    public static MemorySize createBytes(long bytes) {
        return create(bytes, MemoryUnit.BYTES);
    }

    public static MemorySize createKiloBytes(long kiloBytes) {
        return create(kiloBytes, MemoryUnit.KILOBYTES);
    }

    public static MemorySize createMegaBytes(long megaBytes) {
        return create(megaBytes, MemoryUnit.MEGABYTES);
    }

    public static MemorySize createGigaBytes(long gigaBytes) {
        return create(gigaBytes, MemoryUnit.GIGABYTES);
    }

    public static MemorySize create(String mem) {
        long bytes = Long.parseLong(mem.substring(0, mem.length() - 1));
        switch (mem.charAt(mem.length() - 1)) {
            default:
                return new MemorySize(Long.parseLong(mem));
            case 'B':
            case 'b':
                return new MemorySize(bytes);
            case 'K':
            case 'k':
                return new MemorySize(bytes * 1024);
            case 'M':
            case 'm':
                return new MemorySize(bytes * 1024 * 1024);
            case 'G':
            case 'g':
                return new MemorySize(bytes * 1024 * 1024 * 1024);
        }
    }

    public String toString() {
        if (mem < 1024) return mem + "b";
        if (mem < (1024 * 1024)) return (int) (mem / 1024) + "k";
        if (mem < (1024 * 1024 * 1024)) return (int) (mem / (1024 * 1024)) + "m";
        return (int) (mem / (1024 * 1024 * 1024)) + "g";
    }
}
