package com.mcreater.amclcore;

import com.google.gson.reflect.TypeToken;

public class Main {
    public static void main(String[] args) {
        System.out.println(TypeToken.getParameterized(Class.class, String.class).getRawType());
    }
}