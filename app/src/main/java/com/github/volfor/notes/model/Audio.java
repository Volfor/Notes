package com.github.volfor.notes.model;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class Audio {

    public String name;
    public String local;
    public String remote;

    public Audio() {
    }

    public Audio(String local) {
        this.local = local;
    }

    public Audio(String local, String remote) {
        this.local = local;
        this.remote = remote;
    }
}
