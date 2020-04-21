package com.github.mdk.jetbrains.plugin.persiantranslator.translator;

import com.github.mdk.jetbrains.plugin.persiantranslator.cache.CacheManager;

import java.util.concurrent.Callable;

public class Translation implements Callable<String> {

    private final Lang from;
    private final Lang to;
    private final String text;
    private final Translator translator;

    public Translation(String text, Lang from, Lang to, Translator translate) {
        this.text = text;
        this.from = from;
        this.to = to;
        this.translator = translate;
    }

    @Override
    public String call() throws Exception {
        String tr = translator.translate(text, from, to);
        if (tr != null && !tr.isEmpty()) {
            if (tr.equals(GoogleTranslator.CONNECTION_ERROR)) {
                tr = "Please check your internet connection";
                tr += "\n";
                tr += "لطفا اتصال به اینترنت را بررسی کنید";
            } else {
                CacheManager.updateCache(text, tr, from);
            }
        }
        return tr;
    }

}
