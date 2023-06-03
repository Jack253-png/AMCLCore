package com.mcreater.amclcore.nbtlib.nbt;

import com.mcreater.amclcore.nbtlib.nbt.io.*;

import java.io.InputStream;
import java.io.OutputStream;

public class NBTIOUtil {
    public static NBTInput createInput(InputStream stream) {
        return new NBTInputStream(stream);
    }

    public static NBTInput createLEInput(InputStream stream) {
        return new LittleEndianNBTInputStream(stream);
    }

    public static NBTOutput createOutput(OutputStream stream) {
        return new NBTOutputStream(stream);
    }

    public static NBTOutput createLEOutput(OutputStream stream) {
        return new LittleEndianNBTOutputStream(stream);
    }
}
