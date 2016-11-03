package com.github.volfor.notes.model;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.HashMap;
import java.util.Map;

@IgnoreExtraProperties
public class Note {

    public String uid;
    public String author;
    public String title;
    public String text;

    public Note() {
    }

    public Note(String uid, String author, String title, String text) {
        this.uid = uid;
        this.author = author;
        this.title = title;
        this.text = text;
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("uid", uid);
        result.put("author", author);
        result.put("title", title);
        result.put("text", text);

        return result;
    }

}