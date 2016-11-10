package com.github.volfor.notes.note;

import android.support.annotation.ColorInt;
import android.support.annotation.StringRes;

import com.thebluealliance.spectrum.SpectrumDialog;

public interface NoteView {

    void showConflictDialog(String author, String changesPreview);

    void showColorPicker(@ColorInt int selectedColor, SpectrumDialog.OnColorSelectedListener listener);

    void showInformer(@StringRes int resId);

    void finish();
}
