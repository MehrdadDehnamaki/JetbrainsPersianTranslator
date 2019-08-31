package com.github.mdk.plugin.persiantranslator.cache;

import com.github.mdk.plugin.persiantranslator.translator.Lang;

import java.util.concurrent.Callable;

public class CacheFinder implements Callable<String> {

    private final String text;
    private final Lang lang;

    public CacheFinder(String text, Lang lang) {
        this.text = text;
        this.lang = lang;
    }

    @Override
    public String call() throws Exception {
        String result = CacheManager.findFromCash(text, lang);
        if (result != null && !result.isEmpty()) {
            return result;
        } else {
            throw new Exception();
        }
    }
}
