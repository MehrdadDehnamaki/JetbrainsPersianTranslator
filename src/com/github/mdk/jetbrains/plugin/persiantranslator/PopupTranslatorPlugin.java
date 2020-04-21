package com.github.mdk.jetbrains.plugin.persiantranslator;

import com.github.mdk.jetbrains.plugin.persiantranslator.exception.InternalException;
import com.github.mdk.jetbrains.plugin.persiantranslator.exception.NoTargetException;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.editor.Editor;
import org.jetbrains.annotations.NotNull;

public class PopupTranslatorPlugin extends TranslatorPlugin {


    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        final Editor editor = getEditor(event);
        try {
            String translation = getTranslation(event);
            if (translation == null || translation.isEmpty()) {
                showPopupBalloon(ERROR_MSG, editor);
            } else {
                showPopupBalloon(translation, editor);
            }
        } catch (InternalException e) {
            String msg = "Internal error please try again \n" + "خطای داخلی لطفا دوباره امتحان کنید";
            showPopupBalloon(msg, editor);
        } catch (NoTargetException e) {
            String msg = "No text selected \n" + "هیچ متنی انتخاب نشده است";
            showPopupBalloon(msg, editor);
        }
    }
}
