package com.github.volfor.notes.model;

import com.google.firebase.database.IgnoreExtraProperties;

import java.util.List;
import java.util.Map;

@IgnoreExtraProperties
public class Note {

    public String noteId;
    public String title;
    public String text;
    public User author;
    public Map<String, User> contributors;
    public List<String> images;
    public Audio audio;
    public LastChanges lastChanges;
    public int color;

    public Note() {
    }

    public Note(String noteId, String title, String text, User author) {
        this.noteId = noteId;
        this.title = title;
        this.text = text;
        this.author = author;
    }

}