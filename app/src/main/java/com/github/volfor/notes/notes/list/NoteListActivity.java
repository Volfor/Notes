package com.github.volfor.notes.notes.list;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.github.volfor.notes.R;
import com.github.volfor.notes.databinding.ActivityNoteListBinding;
import com.github.volfor.notes.login.LoginActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class NoteListActivity extends AppCompatActivity {

    private ActivityNoteListBinding binding;
    private NoteListViewModel viewModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user == null) {
            startActivity(new Intent(NoteListActivity.this, LoginActivity.class));
            finish();
        }

        binding = DataBindingUtil.setContentView(this, R.layout.activity_note_list);

        viewModel = new NoteListViewModel();
        binding.setViewModel(viewModel);
    }

    @Override
    protected void onDestroy() {
        viewModel.stop();
        super.onDestroy();
    }

}
