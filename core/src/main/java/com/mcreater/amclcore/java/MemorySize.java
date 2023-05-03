package com.mcreater.amclcore.java;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class MemorySize {
    private long mem;

    public static MemorySize create(long mem) {
        return new MemorySize(mem);
    }

    public static MemorySize create(String mem) {
        switch (mem.charAt(mem.length() - 1)) {
            default:
                return new MemorySize(Long.parseLong(mem));
            case 'B':
            case 'b':
                return new MemorySize(Long.parseLong(mem.substring(0, mem.length() - 1)));
            case 'K':
            case 'k':
                return new MemorySize(Long.parseLong(mem.substring(0, mem.length() - 1)) * 1024);
            case 'M':
            case 'm':
                return new MemorySize(Long.parseLong(mem.substring(0, mem.length() - 1)) * 1024 * 1024);
            case 'G':
            case 'g':
                return new MemorySize(Long.parseLong(mem.substring(0, mem.length() - 1)) * 1024 * 1024 * 1024);
        }
    }

    public String toString() {
        if (mem < 1024) return mem + "b";
        if (mem < (1024 * 1024)) return (int) (mem / 1024) + "k";
        if (mem < (1024 * 1024 * 1024)) return (int) (mem / (1024 * 1024)) + "m";
        return (int) (mem / (1024 * 1024 * 1024)) + "g";
    }
}
