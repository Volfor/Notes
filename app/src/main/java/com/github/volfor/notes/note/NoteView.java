package com.github.volfor.notes.note;

import android.support.annotation.StringRes;

public interface NoteView {

    void showConflictDialog(String author, String changesPreview);

    void showInformer(@StringRes int resId);
}
