package org.example.global;

import burp.api.montoya.MontoyaApi;

public class MontoyaApiProvider {
    private static MontoyaApi api;

    private MontoyaApiProvider() {}

    public static void initialize(MontoyaApi montoyaApi) {
        api = montoyaApi;
    }

    public static MontoyaApi get() {
        if (api == null) {
            throw new IllegalStateException("MontoyaApi not initialized yet!");
        }
        return api;
    }
}