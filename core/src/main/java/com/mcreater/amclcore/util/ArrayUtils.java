package com.mcreater.amclcore.util;

import java.util.List;
import java.util.Vector;

public class ArrayUtils {
    public static Integer[] boxed(int[] arr) {
        List<Integer> value = new Vector<>();
        for (int a : arr) value.add(a);
        return value.toArray(new Integer[0]);
    }

    public static Long[] boxed(long[] arr) {
        List<Long> value = new Vector<>();
        for (long a : arr) value.add(a);
        return value.toArray(new Long[0]);
    }

    public static Byte[] boxed(byte[] arr) {
        List<Byte> value = new Vector<>();
        for (byte a : arr) value.add(a);
        return value.toArray(new Byte[0]);
    }

    public static int[] debox(Integer[] integers) {
        int[] arr = new int[integers.length];
        for (int i = 0; i < integers.length; i++) arr[i] = integers[i];
        return arr;
    }

    public static long[] debox(Long[] longs) {
        long[] arr = new long[longs.length];
        for (int i = 0; i < longs.length; i++) arr[i] = longs[i];
        return arr;
    }

    public static byte[] debox(Byte[] longs) {
        byte[] arr = new byte[longs.length];
        for (int i = 0; i < longs.length; i++) arr[i] = longs[i];
        return arr;
    }
}
