# JetbrainsPersianTranslator

CTRL + ALT + W -> Translate the selected text / ترجمه متن انتخاب شده
CTRL + ALT + W -> Replace selected text with translation / جایگزین کردن متن انتخاب به ترجمه

AnAction onPress_ctrl+alt+w = action -> {
    String selectedText = action.getSelectedText();
    if (isPersian(selectedText)){
        String english = translateToEnglish(selectedText);
        showPopupBalloon(english);
    } else {
        String persian = translateToPersian(selectedText);
        showPopupBalloon(persian);
    }   
}


AnAction onPress_ctrl+alt+q = action -> {
    String selectedText = action.getSelectedText();
    if (isPersian(selectedText)){
        String english = translateToEnglish(selectedText);
        replace(english);
    } else {
        String persian = translateToPersian(selectedText);
        replace(persian);
    }   
}
