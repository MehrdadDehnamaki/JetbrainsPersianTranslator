package com.github.mdk.plugin.persiantranslator.translator;

import com.github.mdk.plugin.persiantranslator.cache.CacheManager;

import java.util.concurrent.Callable;

public class Translator implements Callable<String> {

    private final Lang lang;
    private final String text;
    private final GoogleTranslate googleTranslate;

    public Translator(String text, Lang lang, GoogleTranslate googleTranslate) {
        this.text = text;
        this.lang = lang;
        this.googleTranslate = googleTranslate;
    }

    @Override
    public String call() throws Exception {
        String tr;
        if (lang.equals(Lang.FA)) {
            tr = googleTranslate.faToEn(text);
        } else {
            tr = googleTranslate.enToFa(text);
        }
        if (tr != null && !tr.isEmpty()) {
            if (tr.equals(GoogleTranslate.CONNECTION_ERROR)) {
                tr = "لطفا اتصال به اینترنت را بررسی کنید";
            } else {
                CacheManager.updateCache(text, tr, lang);
            }
        }
        return tr;
    }

}
