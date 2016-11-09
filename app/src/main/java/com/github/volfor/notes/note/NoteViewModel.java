package com.github.volfor.notes.note;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.databinding.Bindable;
import android.databinding.ObservableBoolean;
import android.databinding.ObservableField;
import android.databinding.ObservableInt;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.View;
import android.widget.SeekBar;
import android.widget.Toast;

import com.github.volfor.notes.BaseViewModel;
import com.github.volfor.notes.R;
import com.github.volfor.notes.model.Audio;
import com.github.volfor.notes.model.LastChanges;
import com.github.volfor.notes.model.Note;
import com.github.volfor.notes.model.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
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
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import timber.log.Timber;

import static com.github.volfor.notes.Utils.getRealPathFromUri;

public class NoteViewModel extends BaseViewModel {

    public static final int CAMERA_REQUEST = 1006;
    public static final int PICK_IMAGE = 1007;
    public static final int PICK_AUDIO = 1008;

    public static final int REQUEST_FINISH = 1010;

    public Note note;

    public ObservableField<String> title = new ObservableField<>("");
    public ObservableField<String> text = new ObservableField<>("");
    public ObservableField<String> lastChanges = new ObservableField<>("");
    public ObservableBoolean lastChangesVisibility = new ObservableBoolean(false);

    private boolean isFirstStart = true;

    public ObservableBoolean playerBlockVisibility = new ObservableBoolean(false);
    public ObservableBoolean playerLoading = new ObservableBoolean(false);
    public ObservableInt duration = new ObservableInt();
    public ObservableInt elapsed = new ObservableInt();
    public ObservableField<String> songName = new ObservableField<>("Audio file");
    public ObservableBoolean isPlaying = new ObservableBoolean(false);

    public MediaPlayer player;
    private Uri currentAudio;

    public Uri capturedImageUri;

    private DatabaseReference noteReference;
    private ValueEventListener listener;

    private String noteId;
    private List<String> images = new ArrayList<>();
    private NoteImagesAdapter adapter;

    private NoteView view;

    private Handler durationHandler = new Handler();
    private Runnable updateSeekBarTime = new Runnable() {
        public void run() {
            elapsed.set(player.getCurrentPosition());
            durationHandler.postDelayed(this, 100);
        }
    };

    public NoteViewModel(NoteView view, String noteId) {
        this.view = view;
        this.noteId = noteId;
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
                note = dataSnapshot.getValue(Note.class);

                if (note == null) {
                    return;
                }

                if (isFirstStart) {
                    title.set(note.title == null ? "" : note.title);
                    text.set(note.text == null ? "" : note.text);

                    if (note.lastChanges != null && note.lastChanges.time != 0 && note.lastChanges.authorName != null) {
                        lastChanges.set(formLastChangesString(note.lastChanges.time, note.lastChanges.authorName));
                        lastChangesVisibility.set(true);
                    } else {
                        lastChangesVisibility.set(false);
                    }

                    isFirstStart = false;
                } else {
                    if (note.lastChanges != null &&
                            !note.lastChanges.authorId.equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {

                        if (!note.title.equals(title.get()) || !note.text.equals(text.get())) {
                            showConflictDialog();
                            return;
                        }
                    }
                }

                if (note.images == null) {
                    note.images = new ArrayList<>();
                }

                if (adapter != null) {
                    adapter.changeList(note.images);
                }


                if (note.audio != null) {
                    initPlayer(context, note.audio);
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
            if (requestCode == REQUEST_FINISH) {
                view.finish();
                return;
            }

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

                note.images.add(0, path);
                adapter.changeList(note.images);
                try {
                    stream = new FileInputStream(path);
                } catch (FileNotFoundException e) {
                    Timber.e(e, e.getMessage());
                }
            }

            if (requestCode == CAMERA_REQUEST) {
                ref = ref.child("images");
                path = getRealPathFromUri(context, capturedImageUri);

                note.images.add(0, path);
                adapter.changeList(note.images);
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

                Audio audio = new Audio();
                audio.local = data.getData().toString();
                audio.name = path.substring(path.lastIndexOf('/') + 1);
                noteReference.child("audio").setValue(audio);

                initPlayer(context, audio);

                try {
                    stream = new FileInputStream(path);
                } catch (FileNotFoundException e) {
                    Timber.e(e, e.getMessage());
                }
            }

            if (stream != null) {
                String filename = path.substring(path.lastIndexOf('/') + 1);
                ref = ref.child(note.noteId).child(new Date().getTime() + "_" + filename);

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
                            Map<String, Object> mediaMap = new HashMap<>();
                            if (requestCode == PICK_AUDIO) {
                                mediaMap.put("remote", downloadUrl.toString());
                                noteReference.child("audio").updateChildren(mediaMap);
                            } else {
                                note.images.remove(0);
                                note.images.add(0, downloadUrl.toString());
                                adapter.changeList(note.images);
                                mediaMap.put("images", note.images);

                                noteReference.updateChildren(mediaMap);
                            }

                            LastChanges changes = new LastChanges();
                            changes.time = System.currentTimeMillis();
                            changes.authorName = FirebaseAuth.getInstance().getCurrentUser().getDisplayName();
                            changes.authorId = FirebaseAuth.getInstance().getCurrentUser().getUid();

                            noteReference.child("lastChanges").setValue(changes);
                        }
                    }
                });
            }
        }
    }

    @Bindable
    public NoteImagesAdapter getImagesAdapter() {
        adapter = new NoteImagesAdapter(noteId, images);
        return adapter;
    }

    private void initPlayer(Context context, Audio audio) {
        songName.set(audio.name);

        final MediaPlayer.OnCompletionListener onCompletionListener = new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                mediaPlayer.seekTo(0);
                isPlaying.set(false);
            }
        };

        Uri local = null;
        Uri remote = null;
        if (audio.local != null && new File(getRealPathFromUri(context, Uri.parse(audio.local))).exists()) {
            local = Uri.parse(audio.local);
        } else if (audio.remote != null) {
            remote = Uri.parse(audio.remote);
        }

        if (local != null) {
            if (local.equals(currentAudio)) {
                return;
            }

            player = MediaPlayer.create(context, local);
            playerBlockVisibility.set(true);
            isPlaying.set(false);

            elapsed.set(0);
            duration.set(player.getDuration());
            player.setOnCompletionListener(onCompletionListener);

            currentAudio = local;
        } else if (remote != null) {
            if (remote.equals(currentAudio)) {
                return;
            }

            playerLoading.set(true);
            player = new MediaPlayer();

            try {
                player.setDataSource(audio.remote);
            } catch (IOException e) {
                playerLoading.set(false);
                Timber.e(e, e.getMessage());
            }

            player.prepareAsync();
            player.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mediaPlayer) {
                    playerLoading.set(false);
                    playerBlockVisibility.set(true);
                    isPlaying.set(false);

                    elapsed.set(0);
                    duration.set(player.getDuration());
                    player.setOnCompletionListener(onCompletionListener);
                }
            });
            currentAudio = remote;
        }
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

    public void onBackPressed() {
        if (TextUtils.isEmpty(title.get())) {

            return;
        }

        if (!title.get().equals(note.title) || !text.get().equals(note.text)) {
            Map<String, Object> noteMap = new HashMap<>();
            noteMap.put("title", title.get());
            noteMap.put("text", text.get());

            LastChanges changes = new LastChanges();
            changes.time = System.currentTimeMillis();
            changes.authorName = FirebaseAuth.getInstance().getCurrentUser().getDisplayName();
            changes.authorId = FirebaseAuth.getInstance().getCurrentUser().getUid();

            noteMap.put("lastChanges", changes);
            noteReference.updateChildren(noteMap);

            view.showInformer(R.string.saved);
        }

        view.finish();
    }

    public void applyChanges() {
        title.set(note.title);
        text.set(note.text);
    }

    public void mergeChanges() {
        if (!title.get().equals(note.title)) {
            String mergedTitle = title.get() + "/" + note.title;
            title.set(mergedTitle);
        }

        if (!text.get().equals(note.text)) {
            String mergedText = text.get() + "\n<--- " + note.lastChanges.authorName + " changes --->" + "\n" + note.text;
            text.set(mergedText);
        }
    }

    public void shareWithUser(User user) {
        noteReference.child("contributors").child(user.id).setValue(user);
    }

    private String formLastChangesString(long timeInMillis, String author) {
        Date date = new Date();
        date.setTime(timeInMillis);
        String formattedDate = new SimpleDateFormat("MMM d, HH:mm", Locale.US).format(date);

        return String.format("last changes at %s\nby %s", formattedDate, author);
    }

    private void showConflictDialog() {
        String changes = "";
        if (!title.get().equals(note.title)) {
            changes = note.title;
        }

        if (!text.get().equals(note.text)) {
            if (!changes.isEmpty()) {
                changes += "\n\n" + note.text;
            } else {
                changes = note.text;
            }
        }

        changes = changes.substring(0, Math.min(changes.length(), 100));

        view.showConflictDialog(note.lastChanges.authorName, changes);
    }

    public void removeAudio(View v) {
        stopPlayer();
        playerBlockVisibility.set(false);
        currentAudio = null;

        noteReference.child("audio").removeValue();

        Toast.makeText(v.getContext(), R.string.audio_removed, Toast.LENGTH_SHORT).show();
    }

    public void deleteNote() { //TODO only admin can
        noteReference.removeValue();
    }

    @Override
    public void stop() {
        if (listener != null) {
            noteReference.removeEventListener(listener);
        }

        stopPlayer();
    }

}
