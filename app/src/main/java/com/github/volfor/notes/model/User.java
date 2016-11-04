package com.github.volfor.notes.model;

import com.android.annotations.NonNull;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.Exclude;

public class User {

    public String id;
    public String name;
    public String email;

    public User() {
    }

    public User(String id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
    }

    @Exclude
    public static User castToUser(@NonNull FirebaseUser firebaseUser) {
        return new User(firebaseUser.getUid(), firebaseUser.getDisplayName(), firebaseUser.getEmail());
    }

}
