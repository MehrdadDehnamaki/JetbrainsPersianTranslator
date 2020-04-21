package com.github.mdk.jetbrains.plugin.persiantranslator.cache;

import com.github.mdk.jetbrains.plugin.persiantranslator.translator.Lang;

import java.util.HashMap;
import java.util.Map;

public class CacheManager {

    private static final Map<String, String> EN_CACHE = new HashMap<>();
    private static final Map<String, String> FA_CACHE = new HashMap<>();


    static String findFromCash(String key, Lang lang) {
        switch (lang) {
            case EN:
                return EN_CACHE.get(key);
            case FA:
                return FA_CACHE.get(key);
            default:
                return null;
        }
    }

    public static void updateCache(String key, String value, Lang lang) {
        switch (lang) {
            case EN:
                EN_CACHE.put(key, value);
            case FA:
                FA_CACHE.put(key, value);
            default:
        }
    }

}
