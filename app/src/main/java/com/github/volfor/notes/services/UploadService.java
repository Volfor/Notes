package com.github.volfor.notes.services;

import android.app.Service;
import android.content.Intent;
import android.net.Uri;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.github.volfor.notes.model.LastChanges;
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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import timber.log.Timber;

public class UploadService extends Service {

    public static final String EXTRA_NOTE_ID = "NOTE_ID";
    public static final String EXTRA_PATH = "PATH";
    public static final String EXTRA_MEDIA_TYPE = "MEDIA_TYPE";
    public static final int MEDIA_TYPE_AUDIO = 1;
    public static final int MEDIA_TYPE_IMAGES = 2;

    public UploadService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String noteId = intent.getStringExtra(EXTRA_NOTE_ID);
            String path = intent.getStringExtra(EXTRA_PATH);

            final int mediaType = intent.getIntExtra(EXTRA_MEDIA_TYPE, 0);

            if (!TextUtils.isEmpty(noteId) && !TextUtils.isEmpty(path) && mediaType != 0) {
                final DatabaseReference noteReference = FirebaseDatabase.getInstance()
                        .getReference()
                        .child("notes")
                        .child(noteId);

                StorageReference ref = FirebaseStorage.getInstance().getReference();

                InputStream stream = null;
                try {
                    stream = new FileInputStream(path);
                } catch (FileNotFoundException e) {
                    Timber.e(e, e.getMessage());
                }

                if (mediaType == MEDIA_TYPE_AUDIO) {
                    ref = ref.child("audio");
                } else if (mediaType == MEDIA_TYPE_IMAGES) {
                    ref = ref.child("images");
                }

                if (stream != null) {
                    String filename = path.substring(path.lastIndexOf('/') + 1);
                    ref = ref.child(noteId).child(new Date().getTime() + "_" + filename);

                    UploadTask uploadTask = ref.putStream(stream);
                    uploadTask.addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            stopSelf();
                        }
                    }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            final Uri downloadUrl = taskSnapshot.getDownloadUrl();

                            if (downloadUrl != null) {
                                if (mediaType == MEDIA_TYPE_AUDIO) {
                                    Map<String, Object> audioMap = new HashMap<>();
                                    audioMap.put("remote", downloadUrl.toString());
                                    noteReference.child("audio").updateChildren(audioMap);
                                } else {
                                    noteReference.child("images").addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            List<String> images = (ArrayList<String>) dataSnapshot.getValue();
                                            images.add(0, downloadUrl.toString());

                                            Map<String, Object> imagesMap = new HashMap<>();
                                            imagesMap.put("images", images);
                                            noteReference.updateChildren(imagesMap);
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {

                                        }
                                    });
                                }

                                LastChanges changes = new LastChanges();
                                changes.time = System.currentTimeMillis();
                                changes.authorName = FirebaseAuth.getInstance().getCurrentUser().getDisplayName();
                                changes.authorId = FirebaseAuth.getInstance().getCurrentUser().getUid();

                                noteReference.child("lastChanges").setValue(changes);

                                stopSelf();
                            }
                        }
                    });
                }
            }
        }

        return Service.START_NOT_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

}
