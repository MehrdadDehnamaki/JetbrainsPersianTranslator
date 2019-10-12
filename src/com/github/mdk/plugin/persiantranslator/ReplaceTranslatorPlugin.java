package com.github.mdk.plugin.persiantranslator;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public class ReplaceTranslatorPlugin extends TranslatorPlugin {


    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        final String translation = getTranslation(event);
        final Editor editor = getEditor(event);
        if (translation == null || translation.isEmpty()) {
            showPopupBalloon(ERROR_MSG, editor);
        } else {
            final Project project = event.getData(PlatformDataKeys.PROJECT);
            final Caret primaryCaret = editor.getCaretModel().getPrimaryCaret();
            final Document document = editor.getDocument();
            int start = primaryCaret.getSelectionStart();
            int end = primaryCaret.getSelectionEnd();
            WriteCommandAction.runWriteCommandAction(project, () ->
                    document.replaceString(start, end, translation)
            );
            primaryCaret.removeSelection();
        }
    }

}
