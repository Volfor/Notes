package com.github.volfor.notes.root;

import android.databinding.DataBindingUtil;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.github.volfor.notes.R;
import com.github.volfor.notes.databinding.ListItemNoteBinding;
import com.github.volfor.notes.model.Note;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;

public class NoteListAdapter extends FirebaseRecyclerAdapter<Note, NoteListAdapter.ViewHolder> {

    public NoteListAdapter(Query ref) {
        super(Note.class, R.layout.list_item_note, ViewHolder.class, ref);
    }

    public NoteListAdapter(DatabaseReference ref) {
        super(Note.class, R.layout.list_item_note, ViewHolder.class, ref);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        ListItemNoteBinding binding;

        public ViewHolder(View v) {
            super(v);
            binding = DataBindingUtil.bind(v);
        }

        void bindNote(Note note) {
            binding.setViewModel(new NoteItemViewModel(note));
        }
    }

    @Override
    protected void populateViewHolder(ViewHolder holder, Note model, int position) {
        holder.bindNote(model);
    }

}
