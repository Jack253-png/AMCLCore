package com.mcreater.amclcore.nbtlib.common.io;

@FunctionalInterface
public interface ExceptionFunction<T, R, E extends Exception> {

    R accept(T t) throws E;
}
