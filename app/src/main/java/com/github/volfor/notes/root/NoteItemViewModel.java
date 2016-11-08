package com.github.volfor.notes.root;

import android.content.Intent;
import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.view.View;

import com.github.volfor.notes.model.Note;
import com.github.volfor.notes.note.NoteActivity;

public class NoteItemViewModel extends BaseObservable {

    private Note note;

    public NoteItemViewModel(Note note) {
        this.note = note;
    }

    public void onItemClick(View v) {
        Intent intent = new Intent(v.getContext(), NoteActivity.class);
        intent.putExtra("key", note.noteId);
        v.getContext().startActivity(intent);
    }

    @Bindable
    public String getTitle() {
        return note.title;
    }

    @Bindable
    public String getText() {
        return note.text;
    }

}
