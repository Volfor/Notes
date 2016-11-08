package com.github.volfor.notes.note;

import android.content.ContentValues;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.github.volfor.notes.R;
import com.github.volfor.notes.databinding.ActivityNoteBinding;

import static com.github.volfor.notes.note.NoteViewModel.CAMERA_REQUEST;
import static com.github.volfor.notes.note.NoteViewModel.PICK_AUDIO;
import static com.github.volfor.notes.note.NoteViewModel.PICK_IMAGE;

public class NoteActivity extends AppCompatActivity {

    private ActivityNoteBinding binding;
    private NoteViewModel viewModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_note);

        viewModel = new NoteViewModel(getIntent().getStringExtra("key"));
        binding.setViewModel(viewModel);

        viewModel.start(this);

        setSupportActionBar(binding.include.toolbar);

        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        viewModel.onActivityResult(this, requestCode, resultCode, data);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.note_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
            case R.id.attach_photo:
                capturePhoto();
                break;
            case R.id.attach_image:
                pickImage();
                break;
            case R.id.attach_audio:
                pickAudio();
                break;
            default:
        }

        return super.onOptionsItemSelected(item);
    }

    private void capturePhoto() {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "Image File name");
        viewModel.capturedImageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        Intent intentPicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intentPicture.putExtra(MediaStore.EXTRA_OUTPUT, viewModel.capturedImageUri);

        startActivityForResult(intentPicture, CAMERA_REQUEST);
    }

    private void pickImage() {
        Intent pickIntent = new Intent(Intent.ACTION_PICK);
        pickIntent.setType("image/*");

        startActivityForResult(pickIntent, PICK_IMAGE);
    }

    private void pickAudio() {
        Intent pickIntent = new Intent(Intent.ACTION_GET_CONTENT);
        pickIntent.setType("audio/*");

        startActivityForResult(pickIntent, PICK_AUDIO);
    }

    @Override
    protected void onDestroy() {
        viewModel.stop();
        super.onDestroy();
    }
}
