package com.github.mdk.jetbrains.plugin.persiantranslator.translator;

import java.io.IOException;

public interface Translator {

    String translate(String text, Lang from, Lang to) throws IOException;

}
