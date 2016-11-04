package com.github.volfor.notes.model;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.HashMap;
import java.util.Map;

import static android.R.attr.id;

@IgnoreExtraProperties
public class Note {

    public String noteId;
    public String title;
    public String text;
    public User author;
    public Map<String, User> contributors;

    public Note() {
    }

    public Note(String noteId, String title, String text, User author) {
        this.noteId = noteId;
        this.title = title;
        this.text = text;
        this.author = author;
    }

    public Note(String noteId, String title, String text, User author, Map<String, User> contributors) {
        this.noteId = noteId;
        this.title = title;
        this.text = text;
        this.author = author;
        this.contributors = contributors;
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("noteId", id);
        result.put("title", title);
        result.put("text", text);
        result.put("author", author);

        return result;
    }

}