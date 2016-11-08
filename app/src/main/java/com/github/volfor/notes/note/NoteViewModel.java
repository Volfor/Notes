package com.github.volfor.notes.note;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.databinding.Bindable;
import android.databinding.Observable;
import android.databinding.ObservableBoolean;
import android.databinding.ObservableField;
import android.databinding.ObservableInt;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.widget.SeekBar;
import android.widget.Toast;

import com.github.volfor.notes.BaseViewModel;
import com.github.volfor.notes.model.Audio;
import com.github.volfor.notes.model.Note;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import timber.log.Timber;

import static com.github.volfor.notes.Utils.getRealPathFromUri;

public class NoteViewModel extends BaseViewModel {

    public static final int CAMERA_REQUEST = 1006;
    public static final int PICK_IMAGE = 1007;
    public static final int PICK_AUDIO = 1008;

    public ObservableField<Note> note = new ObservableField<>(new Note());
    public ObservableField<String> text = new ObservableField<>("");

    public ObservableBoolean playerBlockVisibility = new ObservableBoolean(false);
    public ObservableInt duration = new ObservableInt();
    public ObservableInt elapsed = new ObservableInt();
    public ObservableField<String> songName = new ObservableField<>("Audio file");
    public ObservableBoolean isPlaying = new ObservableBoolean(false);

    public MediaPlayer player;
    private Uri currentAudio;

    public Uri capturedImageUri;

    private DatabaseReference noteReference;
    private ValueEventListener listener;

    private List<String> images = new ArrayList<>();
    private NoteImagesAdapter adapter;

    private long timeLastCalled;

    private OnPropertyChangedCallback changeCallback = new OnPropertyChangedCallback() {
        @Override
        public void onPropertyChanged(Observable observable, int propertyId) {
            if ((SystemClock.elapsedRealtime() - timeLastCalled) > 2000) {
                noteReference.child("text").setValue(text.get());
                timeLastCalled = SystemClock.elapsedRealtime();
            }
        }
    };

    private Handler durationHandler = new Handler();

    private Runnable updateSeekBarTime = new Runnable() {
        public void run() {
            elapsed.set(player.getCurrentPosition());
            durationHandler.postDelayed(this, 100);
        }
    };

    public NoteViewModel(String noteId) {
        noteReference = FirebaseDatabase.getInstance()
                .getReference()
                .child("notes")
                .child(noteId);
    }

    @Override
    public void start(final Context context) {
        text.addOnPropertyChangedCallback(changeCallback);

        listener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                note.set(dataSnapshot.getValue(Note.class));

                if (note.get() == null) {
                    return;
                }

                if (!note.get().text.equals(text.get())) {
                    text.set(note.get().text);
                }

                adapter.changeList(note.get().images);

                if (note.get().audio != null) {
                    initPlayer(context, note.get().audio);
                } else {
                    playerBlockVisibility.set(false);
                }
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

    public void onActivityResult(Context context, final int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            String path = "";
            InputStream stream = null;
            StorageReference ref = FirebaseStorage.getInstance().getReference();

            if (requestCode == PICK_IMAGE) {
                if (data == null) {
                    // Display an error
                    return;
                }

                ref = ref.child("images");
                path = getRealPathFromUri(context, data.getData());
                try {
                    stream = new FileInputStream(path);
                } catch (FileNotFoundException e) {
                    Timber.e(e, e.getMessage());
                }
            }

            if (requestCode == CAMERA_REQUEST) {
                ref = ref.child("images");
                path = getRealPathFromUri(context, capturedImageUri);
                try {
                    stream = new FileInputStream(new File(path));
                } catch (FileNotFoundException e) {
                    Timber.e(e, e.getMessage());
                }
            }

            if (requestCode == PICK_AUDIO) {
                if (data == null) {
                    // Display an error
                    return;
                }

                ref = ref.child("audios");
                path = getRealPathFromUri(context, data.getData());

                noteReference.child("audio").child("local").setValue(data.getData().toString());
                noteReference.child("audio").child("name").setValue(path.substring(path.lastIndexOf('/') + 1));
                try {
                    stream = new FileInputStream(path);
                } catch (FileNotFoundException e) {
                    Timber.e(e, e.getMessage());
                }
            }

            if (stream != null) {
                String filename = path.substring(path.lastIndexOf('/') + 1);
                ref = ref.child(note.get().noteId).child(new Date().getTime() + "_" + filename);

                UploadTask uploadTask = ref.putStream(stream);
                uploadTask.addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Handle unsuccessful uploads
                    }
                }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Uri downloadUrl = taskSnapshot.getDownloadUrl();
                        taskSnapshot.getStorage().getPath();

                        if (downloadUrl != null) {
                            if (requestCode == PICK_AUDIO) {
                                noteReference.child("audio").child("remote").setValue(downloadUrl.toString());
                            } else {
                                images.add(0, downloadUrl.toString());
                                noteReference.child("images").setValue(images);
                            }
                        }
                    }
                });
            }
        }
    }

    @Bindable
    public NoteImagesAdapter getImagesAdapter() {
        adapter = new NoteImagesAdapter(images);
        return adapter;
    }

    public void removeAudio() {
        stopPlayer();
        playerBlockVisibility.set(false);
        noteReference.child("audio").removeValue();
    }

    private void initPlayer(Context context, Audio audio) {
        songName.set(audio.name);

        Uri uri = null;
        if (audio.local != null && new File(audio.local).exists()) {
            uri = new Uri.Builder().path(audio.local).build();
        } else if (audio.remote != null) {
            uri = Uri.parse(audio.remote);
        }

        if (uri == null || uri.equals(currentAudio)) {
            playerBlockVisibility.set(false);
            return;
        }

        currentAudio = uri;

        if (player != null) {
            player.release();
        }

        playerBlockVisibility.set(true);

        player = MediaPlayer.create(context, uri);
        duration.set(player.getDuration());

        player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                seekTo(0);
            }
        });
    }

    public void play() {
        if (player == null) {
            return;
        }

        if (player.isPlaying()) {
            player.pause();
            isPlaying.set(false);
        } else {
            player.start();
            isPlaying.set(true);
            elapsed.set(player.getCurrentPosition());
            durationHandler.postDelayed(updateSeekBarTime, 100);
        }

    }

    public void seekTo(int millis) {
        if (player != null) {
            player.seekTo(millis);
        }
    }

    private void stopPlayer() {
        if (player != null) {
            player.release();
        }
        durationHandler.removeCallbacks(updateSeekBarTime);
    }

    public String formatToTime(int millis) {
        return String.format(Locale.getDefault(), "%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(millis),
                TimeUnit.MILLISECONDS.toSeconds(millis)
                        - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis)));
    }

    @Bindable
    public SeekBar.OnSeekBarChangeListener getSeekBarChangedListener() {
        return new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    seekTo(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        };
    }

    @Override
    public void stop() {
        if (listener != null) {
            noteReference.removeEventListener(listener);
        }

        stopPlayer();
    }

}
