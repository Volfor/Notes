package com.github.volfor.notes.root;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.volfor.notes.R;
import com.github.volfor.notes.databinding.FragmentContributionsBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class ContributionsFragment extends Fragment {

    private FragmentContributionsBinding binding;
    private NoteListAdapter adapter;

    private Query query;
    private ValueEventListener listener;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_contributions, container, false);
        binding.setViewModel(this);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        query = FirebaseDatabase.getInstance()
                .getReference()
                .child("notes")
                .orderByChild("contributors/" + user.getUid() + "/id")
                .equalTo(user.getUid());

        listener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() == null || dataSnapshot.getChildrenCount() == 0) {
                    binding.sharedNotesList.setVisibility(View.GONE);
                    binding.include.emptyView.setVisibility(View.VISIBLE);
                } else {
                    binding.include.emptyView.setVisibility(View.GONE);
                    binding.sharedNotesList.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        query.addValueEventListener(listener);

        return binding.getRoot();
    }

    public NoteListAdapter getAdapter() {
        adapter = new NoteListAdapter(query);
        return adapter;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (adapter != null) {
            adapter.cleanup();
        }

        query.removeEventListener(listener);
    }
}
