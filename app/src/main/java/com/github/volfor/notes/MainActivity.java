package com.github.volfor.notes;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.github.volfor.notes.databinding.ActivityMainBinding;
import com.github.volfor.notes.model.Note;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            binding.email.setText(user.getEmail());
            binding.name.setText(user.getDisplayName());
        }

        final DatabaseReference database = FirebaseDatabase.getInstance().getReference();

        binding.createNoteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Note note = new Note(user.getUid(), user.getDisplayName(),
                        binding.title.getText().toString(), binding.text.getText().toString());

                database.child("notes").push().setValue(note);
            }
        });
    }

}
