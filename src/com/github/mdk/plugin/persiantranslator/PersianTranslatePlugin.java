package com.github.mdk.plugin.persiantranslator;

import com.github.mdk.plugin.persiantranslator.cache.CacheFinder;
import com.github.mdk.plugin.persiantranslator.cache.CacheManager;
import com.github.mdk.plugin.persiantranslator.translator.GoogleTranslate;
import com.github.mdk.plugin.persiantranslator.translator.Lang;
import com.github.mdk.plugin.persiantranslator.translator.Translator;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.CaretModel;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.ui.popup.Balloon;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.util.TextRange;
import com.intellij.ui.Gray;
import com.intellij.ui.JBColor;
import com.intellij.ui.popup.PopupFactoryImpl;
import org.apache.http.util.TextUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PersianTranslatePlugin extends AnAction {


    private static final String ICON_PATH = "/icons/translate.png";
    private static final String ERROR_MSG = "خطا در ترجمه!";
    private static final Icon icon = new ImageIcon(ICON_PATH);
    private static final GoogleTranslate googleTranslate = new GoogleTranslate();
    private static final ExecutorService executorService = Executors.newFixedThreadPool(3);

    private long mLatestClickTime;

    public PersianTranslatePlugin() {
        super(IconLoader.getIcon(ICON_PATH));
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        if (!isFastClick()) {
            getTranslation(e);
        }
    }

    private void getTranslation(AnActionEvent event) {
        Editor editor = event.getData(PlatformDataKeys.EDITOR);
        if (editor == null) {
            return;
        }
        SelectionModel model = editor.getSelectionModel();
        String selectedText = model.getSelectedText();
        if (TextUtils.isEmpty(selectedText) || selectedText.contains(CacheManager.CACHE_SEPARATOR)) {
            selectedText = getCurrentWords(editor);
            if (TextUtils.isEmpty(selectedText)) {
                return;
            }
        }
        String queryText = strip(addBlanks(selectedText));
        if (queryText.charAt(0) == ' '){
            queryText = queryText.substring(1);
        }

        String tr = null;
        Lang lang = isFarsi(queryText) ? Lang.FA : Lang.EN;
        ArrayList<Callable<String>> callable = new ArrayList<>();
        callable.add(new CacheFinder(queryText, lang));
        callable.add(new Translator(queryText, lang, googleTranslate));
        try {
            tr = executorService.invokeAny(callable);
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        if (tr == null) {
            showPopupBalloon(ERROR_MSG, editor);
        } else {
            showPopupBalloon(tr, editor);
        }
    }

    private String getCurrentWords(Editor editor) {
        Document document = editor.getDocument();
        CaretModel caretModel = editor.getCaretModel();
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
    }

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

    private boolean isFastClick() {
        long time = System.currentTimeMillis();
        long timeD = time - mLatestClickTime;
        if (0 < timeD && timeD < 1000) {
            return true;
        }
        mLatestClickTime = time;
        return false;
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

    private void showPopupBalloon(final String result, Editor editor) {
        ApplicationManager.getApplication().invokeLater(() -> {
            editor.putUserData(PopupFactoryImpl.ANCHOR_POPUP_POSITION, null);
            JBPopupFactory factory = JBPopupFactory.getInstance();
            factory.createHtmlTextBalloonBuilder(result, icon, new JBColor(Gray._242, Gray._0), null)
                    .createBalloon().show(factory.guessBestPopupLocation(editor), Balloon.Position.below);
        });
    }
}
