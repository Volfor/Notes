package com.github.volfor.notes.info;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.view.MenuItem;
import android.view.View;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.github.volfor.notes.R;
import com.github.volfor.notes.databinding.ActivityInfoBinding;
import com.github.volfor.notes.model.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class InfoActivity extends AppCompatActivity implements InfoView {

    private ActivityInfoBinding binding;
    private FirebaseRecyclerAdapter adapter;

    private User author;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_info);

        author = (User) getIntent().getSerializableExtra("author");
        binding.setAuthor(author);

        setSupportActionBar(binding.include.toolbar);

        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setDisplayHomeAsUpEnabled(true);
        }

        final DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference()
                .child("notes")
                .child(getIntent().getStringExtra("key"));

        ref.child("contributors").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() == null || dataSnapshot.getChildrenCount() == 0) {
                    binding.contributorsBlock.setVisibility(View.GONE);
                } else {
                    binding.contributorsBlock.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        adapter = new ContributorsAdapter(this, author.id, ref.child("contributors"));
        binding.contributorsList.setHasFixedSize(true);
        binding.contributorsList.setNestedScrollingEnabled(false);
        binding.contributorsList.setLayoutManager(new LinearLayoutManager(binding.contributorsList.getContext()) {
            @Override
            public boolean canScrollVertically() {
                return false;
            }
        });
        binding.contributorsList.setAdapter(adapter);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
            default:
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (adapter != null) {
            adapter.cleanup();
        }
    }
}
