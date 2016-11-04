package com.github.volfor.notes.notes;

import android.content.Context;
import android.content.Intent;
import android.databinding.ObservableField;
import android.view.View;
import android.widget.Toast;

import com.github.volfor.notes.BaseViewModel;
import com.github.volfor.notes.ContributionsFragment;
import com.github.volfor.notes.model.Note;
import com.github.volfor.notes.model.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import timber.log.Timber;

public class NoteViewModel extends BaseViewModel {

    public ObservableField<String> title = new ObservableField<>("");
    public ObservableField<String> text = new ObservableField<>("");

    private DatabaseReference noteReference;
    private ValueEventListener listener;

    public NoteViewModel(String noteId) {
        noteReference = FirebaseDatabase.getInstance()
                .getReference()
                .child("notes")
                .child(noteId);
    }

    @Override
    public void start(final Context context) {
        listener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Note note = dataSnapshot.getValue(Note.class);

                title.set(note.title);
                text.set(note.text);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Timber.w(databaseError.toException(), "loadPost:onCancelled");

                Toast.makeText(context, "Failed to load note.", Toast.LENGTH_SHORT).show();
            }
        };

        noteReference.addValueEventListener(listener);
    }

    public void saveChanges() {
        noteReference.child("title").setValue(title.get().trim());
        noteReference.child("text").setValue(text.get().trim());
    }

    public void share() {
        User user = new User("Jh3R9jNBoEcejyUSMjFoJLnQ09D3", "Igor Trifonov", "trigor74@gmail.com");
        noteReference.child("contributors").child("Jh3R9jNBoEcejyUSMjFoJLnQ09D3").setValue(user);
    }

    public void open(View v) {
        Intent intent = new Intent(v.getContext(), ContributionsFragment.class);
        v.getContext().startActivity(intent);

    }

    @Override
    public void stop() {
        if (listener != null) {
            noteReference.removeEventListener(listener);
        }
    }
}
