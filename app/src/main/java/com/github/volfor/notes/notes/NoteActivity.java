package com.github.volfor.notes.notes;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.github.volfor.notes.R;
import com.github.volfor.notes.databinding.ActivityNoteBinding;

public class NoteActivity extends AppCompatActivity {

    ActivityNoteBinding binding;
    NoteViewModel viewModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_note);

        viewModel = new NoteViewModel(getIntent().getStringExtra("key"));
        binding.setViewModel(viewModel);
    }

    @Override
    protected void onStart() {
        super.onStart();
        viewModel.start(this);
    }

    @Override
    protected void onStop() {
        viewModel.stop();
        super.onStop();
    }
}
