package com.github.volfor.notes;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.github.volfor.notes.databinding.FragmentContributionsBinding;
import com.github.volfor.notes.model.Note;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

public class ContributionsFragment extends Fragment {

    private FragmentContributionsBinding binding;
    private FirebaseRecyclerAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_contributions, container, false);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.sharedNotesList.setHasFixedSize(true);
        binding.sharedNotesList.setLayoutManager(new LinearLayoutManager(getContext()));

        DatabaseReference database = FirebaseDatabase.getInstance().getReference().child("notes");
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

//        Query query = database.orderByChild("contributors/" + user.getUid()).equalTo(true);
        Query query = database.orderByChild("contributors/" + user.getUid() + "/id").equalTo(user.getUid());

        adapter = new FirebaseRecyclerAdapter<Note, ViewHolder>(Note.class, android.R.layout.two_line_list_item, ViewHolder.class, query) {
            @Override
            public void populateViewHolder(ViewHolder holder, Note sharedNote, int position) {
                holder.setTitle(sharedNote.title);
                holder.setText(sharedNote.text);
            }
        };

        binding.sharedNotesList.setAdapter(adapter);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        View view;

        public ViewHolder(View itemView) {
            super(itemView);
            view = itemView;
        }

        public void setTitle(String name) {
            TextView field = (TextView) view.findViewById(android.R.id.text1);
            field.setText(name);
        }

        public void setText(String text) {
            TextView field = (TextView) view.findViewById(android.R.id.text2);
            field.setText(text);
        }
    }
}
