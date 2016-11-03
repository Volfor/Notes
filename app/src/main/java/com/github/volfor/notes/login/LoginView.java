package com.github.volfor.notes.login;

import android.content.Intent;

public interface LoginView {

    void showProgressDialog();

    void hideProgressDialog();

    void startActivityForResult(Intent intent, int requestCode);

    void startNoteListActivity();

    void showMessage(String text);

}
