package com.mcreater.amclcore;

import com.google.gson.reflect.TypeToken;
import com.mcreater.amclcore.account.auth.OAuth;

import java.io.IOException;
import java.net.URISyntaxException;

public class Main {
    public static void main(String[] args) throws URISyntaxException, IOException {
        System.out.println(TypeToken.getParameterized(Class.class, String.class).getRawType());
        OAuth.MICROSOFT.createDeviceToken();
    }
}