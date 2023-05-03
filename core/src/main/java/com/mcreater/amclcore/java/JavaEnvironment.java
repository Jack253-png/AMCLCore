package com.mcreater.amclcore.java;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.File;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class JavaEnvironment {
    @Getter
    private File executable;

    public static JavaEnvironment create(File executable) {
        return new JavaEnvironment(executable);
    }
}
