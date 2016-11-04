package com.github.volfor.notes.notes.create;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.github.volfor.notes.R;
import com.github.volfor.notes.databinding.ActivityCreateNoteBinding;
import com.github.volfor.notes.model.Note;
import com.github.volfor.notes.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class CreateNoteActivity extends AppCompatActivity {

    private ActivityCreateNoteBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_create_note);

        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            binding.email.setText(user.getEmail());
            binding.name.setText(user.getDisplayName());
        }

        final DatabaseReference database = FirebaseDatabase.getInstance().getReference();

        binding.createNoteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String key = database.child("notes").push().getKey();

                Note note = new Note(key, binding.title.getText().toString(), binding.text.getText().toString(), User.castToUser(user));

                database.child("notes").child(key).setValue(note);
            }
        });
    }

}
