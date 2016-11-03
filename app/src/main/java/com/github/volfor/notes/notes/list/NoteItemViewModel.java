package com.github.volfor.notes.notes.list;

import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.view.View;
import android.widget.Toast;

import com.github.volfor.notes.model.Note;

public class NoteItemViewModel extends BaseObservable {

    private Note note;

    public NoteItemViewModel(Note note) {
        this.note = note;
    }

    public void onItemClick(View v) {
//        Intent intent = new Intent(v.getContext(), NoteActivity.class);
//        v.getContext().startActivity(intent);
//        intent.putExtra("key", key);
//        intent.putExtra("note", note);
        Toast.makeText(v.getContext(), "Item " + note.title + " clicked", Toast.LENGTH_SHORT).show();
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
