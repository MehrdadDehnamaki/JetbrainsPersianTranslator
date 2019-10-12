package com.github.mdk.plugin.persiantranslator;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.editor.Editor;
import org.jetbrains.annotations.NotNull;

public class PopupTranslatorPlugin extends TranslatorPlugin {


    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        String translation = getTranslation(event);
        final Editor editor = getEditor(event);
        if (translation == null || translation.isEmpty()) {
            showPopupBalloon(ERROR_MSG, editor);
        } else {
            showPopupBalloon(translation, editor);
        }
    }
}
