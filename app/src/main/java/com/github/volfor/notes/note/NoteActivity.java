package com.github.volfor.notes.note;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import com.github.volfor.notes.R;
import com.github.volfor.notes.databinding.ActivityNoteBinding;
import com.github.volfor.notes.info.InfoActivity;
import com.github.volfor.notes.model.User;
import com.github.volfor.notes.sharing.UsersAutoCompleteAdapter;
import com.google.firebase.auth.FirebaseAuth;

import static com.github.volfor.notes.note.NoteViewModel.CAMERA_REQUEST;
import static com.github.volfor.notes.note.NoteViewModel.PICK_AUDIO;
import static com.github.volfor.notes.note.NoteViewModel.PICK_IMAGE;

public class NoteActivity extends AppCompatActivity implements NoteView {

    private ActivityNoteBinding binding;
    private NoteViewModel viewModel;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_note);

        viewModel = new NoteViewModel(this, getIntent().getStringExtra("key"));
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

        User author = (User) getIntent().getSerializableExtra("author");
        if (!author.id.equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
            menu.findItem(R.id.delete_note).setVisible(false);
            menu.findItem(R.id.share_note).setVisible(false);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                viewModel.saveNote();
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
            case R.id.share_note:
                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);

                LayoutInflater inflater = this.getLayoutInflater();
                View dialogView = inflater.inflate(R.layout.share_alert_dialog, null);
                dialogBuilder.setView(dialogView);
                dialogBuilder.setTitle(R.string.share_text);

                final AlertDialog alertDialog = dialogBuilder.create();
                final AutoCompleteTextView autocomplete = (AutoCompleteTextView) dialogView.findViewById(R.id.autocomplete);

                autocomplete.setAdapter(new UsersAutoCompleteAdapter(this));
                autocomplete.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                        User user = (User) adapterView.getItemAtPosition(position);
                        autocomplete.setText(user.name);
                        viewModel.shareWithUser(user);

                        alertDialog.dismiss();

                        Toast.makeText(NoteActivity.this, R.string.shared, Toast.LENGTH_SHORT).show();
                    }
                });

                alertDialog.show();
                break;
            case R.id.note_info:
                Intent intent = new Intent(NoteActivity.this, InfoActivity.class);
                intent.putExtra("key", getIntent().getStringExtra("key"));
                intent.putExtra("author", getIntent().getSerializableExtra("author"));
                startActivity(intent);
                break;
            case R.id.delete_note:
                new AlertDialog.Builder(this)
                        .setMessage(R.string.delete_note_message)
                        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                viewModel.deleteNote();
                                Toast.makeText(NoteActivity.this, R.string.deleted, Toast.LENGTH_SHORT).show();
                                finish();
                            }
                        })
                        .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // do nothing
                            }
                        })
                        .show();
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
    public void showConflictDialog(String author, String changesPreview) {
        new AlertDialog.Builder(this)
                .setTitle(author + " made changes:")
                .setMessage(changesPreview)
                .setPositiveButton(R.string.apply, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        viewModel.applyChanges();
                        Toast.makeText(NoteActivity.this, R.string.applied, Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton(R.string.merge, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        viewModel.mergeChanges();
                        Toast.makeText(NoteActivity.this, R.string.merged, Toast.LENGTH_SHORT).show();
                    }
                })
                .setNeutralButton(R.string.discard, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // do nothing
                    }
                })
                .show();
    }

    @Override
    public void showInformer(@StringRes int resId) {
        Toast.makeText(this, resId, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        viewModel.stop();
        super.onDestroy();
    }
}
