package com.github.volfor.notes.model;

import com.android.annotations.NonNull;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.Exclude;

import java.io.Serializable;

public class User implements Serializable {

    public String id;
    public String name;
    public String email;
    public String avatar;

    public User() {
    }

    public User(String id, String name, String email, String avatar) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.avatar = avatar;
    }

    @Exclude
    public static User castToUser(@NonNull FirebaseUser firebaseUser) {
        return new User(firebaseUser.getUid(), firebaseUser.getDisplayName(),
                firebaseUser.getEmail(), firebaseUser.getPhotoUrl().toString());
    }

}
