package com.github.mdk.jetbrains.plugin.persiantranslator;

import com.github.mdk.jetbrains.plugin.persiantranslator.cache.CacheFinder;
import com.github.mdk.jetbrains.plugin.persiantranslator.exception.InternalException;
import com.github.mdk.jetbrains.plugin.persiantranslator.exception.NoTargetException;
import com.github.mdk.jetbrains.plugin.persiantranslator.translator.GoogleTranslator;
import com.github.mdk.jetbrains.plugin.persiantranslator.translator.Lang;
import com.github.mdk.jetbrains.plugin.persiantranslator.translator.Translation;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.ui.popup.Balloon;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.util.IconLoader;
import com.intellij.ui.Gray;
import com.intellij.ui.JBColor;
import com.intellij.ui.popup.PopupFactoryImpl;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

abstract class TranslatorPlugin extends AnAction {

    static final String ERROR_MSG = "خطا در ترجمه!";
    private static final String ICON_PATH = "/icons/translate.png";
    private static final Icon icon = new ImageIcon(ICON_PATH);
    private static final GoogleTranslator googleTranslate = new GoogleTranslator();
    private static final ExecutorService executorService = Executors.newFixedThreadPool(3);

    TranslatorPlugin() {
        super(IconLoader.getIcon(ICON_PATH));
    }


    String getTranslation(AnActionEvent event) {
        final String targetText = getTargetText(event);
        String translated;

        if (targetText != null && !targetText.isEmpty()) {
            Lang from = isFarsi(targetText) ? Lang.FA : Lang.EN;
            Lang to = isFarsi(targetText) ? Lang.EN : Lang.FA;
            ArrayList<Callable<String>> callable = new ArrayList<>();
            callable.add(new CacheFinder(targetText, from));
            callable.add(new Translation(targetText, from, to, googleTranslate));
            try {
                translated = executorService.invokeAny(callable);
            } catch (InterruptedException | ExecutionException e) {
                throw new InternalException();
            }
        } else {
            throw new NoTargetException();
        }
        return translated;
    }

    private String getTargetText(AnActionEvent event) {
        final Editor editor = getEditor(event);
        if (editor == null) {
            return null;
        }
        final SelectionModel model = editor.getSelectionModel();
        String selectedText = model.getSelectedText();
        if (selectedText == null || selectedText.isEmpty()) {
                return null;
        }
        String targetText = strip(addBlanks(selectedText));
        if (targetText.charAt(0) == ' ') {
            targetText = targetText.substring(1);
        }
        return targetText;
    }

    /*private String getCurrentWords(Editor editor) {
        final Document document = editor.getDocument();
        final CaretModel caretModel = editor.getCaretModel();
        int caretOffset = caretModel.getOffset();
        int lineNum = document.getLineNumber(caretOffset);
        int lineStartOffset = document.getLineStartOffset(lineNum);
        int lineEndOffset = document.getLineEndOffset(lineNum);
        String lineContent = document.getText(new TextRange(lineStartOffset, lineEndOffset));
        char[] chars = lineContent.toCharArray();
        int start = 0, end = 0, cursor = caretOffset - lineStartOffset;

        if (!Character.isLetter(chars[cursor])) {
            return null;
        }

        for (int ptr = cursor; ptr >= 0; ptr--) {
            if (!Character.isLetter(chars[ptr])) {
                start = ptr + 1;
                break;
            }
        }

        int lastLetter = 0;
        for (int ptr = cursor; ptr < lineEndOffset - lineStartOffset; ptr++) {
            lastLetter = ptr;
            if (!Character.isLetter(chars[ptr])) {
                end = ptr;
                break;
            }
        }
        if (end == 0) {
            end = lastLetter + 1;
        }
        return new String(chars, start, end - start);
    }*/

    private String addBlanks(String str) {
        String temp = str.replaceAll("_", " ");
        if (temp.equals(temp.toUpperCase())) {
            return temp;
        }
        return temp.replaceAll("([A-Z]+)", " $0");
    }

    private String strip(String str) {
        return str.replaceAll("/\\*+", "").replaceAll("\\*+/", "")
                .replaceAll("\\*", "").replaceAll("//+", "")
                .replaceAll("\r\n", " ").replaceAll("\\s+", " ");
    }


    private boolean isFarsi(String strName) {
        char[] cs = strName.toCharArray();
        for (char c : cs) {
            if (isFarsi(c)) {
                return true;
            }
        }
        return false;
    }

    private boolean isFarsi(char c) {
        Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);
        return ub == Character.UnicodeBlock.OLD_PERSIAN || ub == Character.UnicodeBlock.ARABIC;
    }

    void showPopupBalloon(final String result, Editor editor) {
        ApplicationManager.getApplication().invokeLater(() -> {
            editor.putUserData(PopupFactoryImpl.ANCHOR_POPUP_POSITION, null);
            JBPopupFactory factory = JBPopupFactory.getInstance();
            String title = isFarsi(result) ? "ترجمه" : "Translation";
            Balloon balloon = factory.createHtmlTextBalloonBuilder(result, icon, new JBColor(Gray._242, Gray._0), null)
                    .setShadow(true).setCloseButtonEnabled(true).setTitle(title)
                    .createBalloon();
            balloon.show(factory.guessBestPopupLocation(editor), Balloon.Position.below);
        });
    }

    Editor getEditor(AnActionEvent event) {
        return event.getData(PlatformDataKeys.EDITOR);
    }
}
