package com.github.volfor.notes.notes.list;

import android.content.Context;
import android.content.Intent;
import android.databinding.BindingAdapter;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.github.volfor.notes.BaseViewModel;
import com.github.volfor.notes.notes.create.CreateNoteActivity;
import com.google.firebase.database.FirebaseDatabase;

public class NoteListViewModel extends BaseViewModel {

    private NoteListAdapter adapter;

    public NoteListAdapter getAdapter() {
        adapter = new NoteListAdapter(FirebaseDatabase.getInstance().getReference().child("notes"));
        return adapter;
    }

    @Override
    public void start(Context context) {

    }

    public void onCreateNoteClick(View v) {
        Intent intent = new Intent(v.getContext(), CreateNoteActivity.class);
        v.getContext().startActivity(intent);
    }

    @Override
    public void stop() {
        if (adapter != null) {
            adapter.cleanup();
        }
    }

    @BindingAdapter({"adapter"})
    public static void bindAdapter(RecyclerView view, RecyclerView.Adapter adapter) {
        view.setHasFixedSize(true);
        view.setLayoutManager(new LinearLayoutManager(view.getContext()));
        view.setAdapter(adapter);
    }
}
