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
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

public class ContributionsFragment extends Fragment {

    private FragmentContributionsBinding binding;
    private NoteListAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_contributions, container, false);
        binding.setViewModel(this);

        return binding.getRoot();
    }

    public NoteListAdapter getAdapter() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

//          Query query = database.orderByChild("contributors/" + user.getUid()).equalTo(true);
        Query query = FirebaseDatabase.getInstance()
                .getReference()
                .child("notes")
                .orderByChild("contributors/" + user.getUid() + "/id")
                .equalTo(user.getUid());

        adapter = new NoteListAdapter(query);

        return adapter;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (adapter != null) {
            adapter.cleanup();
        }
    }
}