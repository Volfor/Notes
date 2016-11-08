package com.github.volfor.notes.model;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class LastChanges {

    public String authorId;
    public String authorName;
    public long time;

    public LastChanges() {
    }

    public LastChanges(String authorId, String authorName, long time) {
        this.authorId = authorId;
        this.authorName = authorName;
        this.time = time;
    }
}
