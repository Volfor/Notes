package com.github.volfor.notes.root;

import android.content.Intent;
import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.support.annotation.ColorInt;
import android.view.View;

import com.github.volfor.notes.model.Note;
import com.github.volfor.notes.note.NoteActivity;
import com.google.firebase.auth.FirebaseAuth;

public class NoteItemViewModel extends BaseObservable {

    private Note note;

    public NoteItemViewModel(Note note) {
        this.note = note;
    }

    public void onItemClick(View v) {
        Intent intent = new Intent(v.getContext(), NoteActivity.class);
        intent.putExtra("key", note.noteId);
        intent.putExtra("author", note.author);
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

    @Bindable
    @ColorInt
    public int getColor() {
        return note.color;
    }

    @Bindable
    public boolean getContributors() {
        return note.contributors != null && note.author.id.equals(FirebaseAuth.getInstance().getCurrentUser().getUid());

    }

    @Bindable
    public boolean getImages() {
        return note.images != null && !note.images.isEmpty();
    }

    @Bindable
    public boolean getAudio() {
        return note.audio != null;
    }

}
