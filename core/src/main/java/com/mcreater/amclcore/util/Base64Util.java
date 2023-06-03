package com.mcreater.amclcore.util;

import java.nio.charset.Charset;
import java.util.Base64;

public class Base64Util {
    public static byte[] decode(String data) {
        return Base64.getDecoder().decode(data);
    }

    public static String decode(String data, Charset charset) {
        return new String(decode(data), charset);
    }

    public static byte[] encode(String raw) {
        return Base64.getEncoder().encode(raw.getBytes());
    }

    public static String encode(String raw, Charset charset) {
        return new String(encode(raw), charset);
    }
}
