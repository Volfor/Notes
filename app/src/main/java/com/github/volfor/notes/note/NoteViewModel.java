package com.github.volfor.notes.note;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.databinding.Bindable;
import android.databinding.ObservableBoolean;
import android.databinding.ObservableField;
import android.databinding.ObservableInt;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.support.annotation.ColorInt;
import android.text.TextUtils;
import android.view.View;
import android.widget.SeekBar;
import android.widget.Toast;

import com.github.volfor.notes.BaseViewModel;
import com.github.volfor.notes.R;
import com.github.volfor.notes.Utils;
import com.github.volfor.notes.model.Audio;
import com.github.volfor.notes.model.LastChanges;
import com.github.volfor.notes.model.Note;
import com.github.volfor.notes.model.User;
import com.github.volfor.notes.services.UploadService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.thebluealliance.spectrum.SpectrumDialog;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import timber.log.Timber;

import static com.github.volfor.notes.Utils.getAudioPathFromContentUri;
import static com.github.volfor.notes.Utils.getImagePathFromUri;

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
    public ObservableInt color = new ObservableInt(-1);

    private boolean isFirstStart = true;

    public ObservableBoolean playerBlockVisibility = new ObservableBoolean(false);
    public ObservableBoolean playerLoading = new ObservableBoolean(false);
    public ObservableInt duration = new ObservableInt();
    public ObservableInt elapsed = new ObservableInt();
    public ObservableField<String> songName = new ObservableField<>("Audio file");
    public ObservableBoolean isPlaying = new ObservableBoolean(false);

    public MediaPlayer player;
    private String currentAudio;

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

                color.set(note.color);

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
            if (requestCode == PICK_IMAGE) {
                if (data == null) {
                    view.showInformer(R.string.attachment_error);
                    return;
                }

                path = getImagePathFromUri(context, data.getData());

                if (TextUtils.isEmpty(path)) {
                    view.showInformer(R.string.attachment_error);
                    return;
                }

                note.images.add(0, path);
                adapter.changeList(note.images);
            }

            if (requestCode == CAMERA_REQUEST) {
                path = getImagePathFromUri(context, capturedImageUri);

                if (TextUtils.isEmpty(path)) {
                    view.showInformer(R.string.attachment_error);
                    return;
                }

                note.images.add(0, path);
                adapter.changeList(note.images);
            }

            if (requestCode == PICK_AUDIO) {
                if (data == null) {
                    view.showInformer(R.string.attachment_error);
                    return;
                }

                try {
                    path = getAudioPathFromContentUri(context, data.getData());
                } catch (Exception e) {
                    Timber.e(e, e.getMessage());
                }

                if (TextUtils.isEmpty(path)) {
                    view.showInformer(R.string.attachment_error);
                    return;
                }

                String ext = path.substring(path.lastIndexOf('.') + 1);
                if (!ext.equals("mp3") && !ext.equals("flac") && !ext.equals("ogg")
                        && !ext.equals("wav") && !ext.equals("m4a")) {
                    view.showInformer(R.string.format_not_supported);
                    return;
                }

                Audio audio = new Audio();
                audio.local = data.getData().toString();
                audio.name = path.substring(path.lastIndexOf('/') + 1);
                noteReference.child("audio").setValue(audio);

                initPlayer(context, audio);
            }

            if (!TextUtils.isEmpty(path)) {
                Intent uploadServiceIntent = new Intent(context, UploadService.class);
                if (requestCode == PICK_AUDIO) {
                    uploadServiceIntent.putExtra(UploadService.EXTRA_MEDIA_TYPE, UploadService.MEDIA_TYPE_AUDIO);
                } else {
                    uploadServiceIntent.putExtra(UploadService.EXTRA_MEDIA_TYPE, UploadService.MEDIA_TYPE_IMAGES);
                }

                uploadServiceIntent.putExtra(UploadService.EXTRA_NOTE_ID, noteId);
                uploadServiceIntent.putExtra(UploadService.EXTRA_PATH, path);

                context.getApplicationContext().startService(uploadServiceIntent);
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

        String path = null;
        boolean isLocal = false;

        try {
            if (audio.local != null) {
                isLocal = new File(getAudioPathFromContentUri(context, Uri.parse(audio.local))).exists();
            }
        } catch (Exception e) {
            Timber.e(e, e.getMessage());
        }

        if (isLocal) {
            path = audio.local;
        } else if (audio.remote != null) {
            path = audio.remote;
        }

        if (TextUtils.isEmpty(path) || path.equals(currentAudio)) {
            return;
        }

        playerLoading.set(true);

        if (player != null) {
            player.release();
        }

        player = new MediaPlayer();
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);

        try {
            Utils.setMediaPlayerDataSource(context, player, path);
        } catch (Exception e) {
            Timber.e(e, e.getMessage());

            playerLoading.set(false);
            view.showInformer(R.string.error_loading_audio);
            return;
        }

        player.prepareAsync();

        final String finalPath = path;
        player.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                playerLoading.set(false);
                playerBlockVisibility.set(true);
                isPlaying.set(false);

                elapsed.set(0);
                duration.set(player.getDuration());
                player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mediaPlayer) {
                        mediaPlayer.seekTo(0);
                        isPlaying.set(false);
                    }
                });

                currentAudio = finalPath;
            }
        });

        player.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
                playerLoading.set(false);
                view.showInformer(R.string.error_loading_audio);
                return false;
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

    public void saveNote() {
        if (TextUtils.isEmpty(title.get()) && TextUtils.isEmpty(text.get())) {
            if ((note.images == null || note.images.isEmpty()) && note.audio == null) {
                noteReference.removeValue();
                return;
            }
        }

//        if (!title.get().equals(note.title) || !text.get().equals(note.text) || color.get() != note.color) {
        if (!title.get().equals(note.title) || !text.get().equals(note.text)) {
            Map<String, Object> noteMap = new HashMap<>();
            noteMap.put("title", title.get().trim());
            noteMap.put("text", text.get().trim());

            LastChanges changes = new LastChanges();
            changes.time = System.currentTimeMillis();
            changes.authorName = FirebaseAuth.getInstance().getCurrentUser().getDisplayName();
            changes.authorId = FirebaseAuth.getInstance().getCurrentUser().getUid();

            noteMap.put("lastChanges", changes);
            noteReference.updateChildren(noteMap);

            view.showInformer(R.string.saved);
        }
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

    public void pickColor() {
        SpectrumDialog.OnColorSelectedListener listener = new SpectrumDialog.OnColorSelectedListener() {
            @Override
            public void onColorSelected(boolean positiveResult, @ColorInt int chosenColor) {
                color.set(chosenColor);
                noteReference.child("color").setValue(chosenColor);
            }
        };

        view.showColorPicker(color.get(), listener);
    }

    public void shareWithUser(User user) {
        saveNote();
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

    public void deleteNote() {
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
