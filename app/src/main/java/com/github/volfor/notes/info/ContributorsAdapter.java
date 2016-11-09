package com.github.volfor.notes.info;

import android.databinding.DataBindingUtil;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.github.volfor.notes.R;
import com.github.volfor.notes.databinding.ItemContributorBinding;
import com.github.volfor.notes.model.User;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;

public class ContributorsAdapter extends FirebaseRecyclerAdapter<User, ContributorsAdapter.ViewHolder> {

    private InfoView view;
    private Query ref;
    private String authorId;

    public ContributorsAdapter(InfoView view, String authorId, DatabaseReference ref) {
        super(User.class, R.layout.item_contributor, ViewHolder.class, ref);
        this.view = view;
        this.ref = ref;
        this.authorId = authorId;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        ItemContributorBinding binding;

        public ViewHolder(View v) {
            super(v);
            binding = DataBindingUtil.bind(v);
        }

        void bindContributor(InfoView view, Query ref, User user, String authorId) {
            binding.setViewModel(new UserItemViewModel(view, ref, user, authorId));
        }
    }

    @Override
    protected void populateViewHolder(ViewHolder holder, User model, int position) {
        holder.bindContributor(view, ref, model, authorId);
    }

}
