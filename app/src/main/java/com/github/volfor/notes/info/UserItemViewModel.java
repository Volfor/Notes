package com.github.volfor.notes.info;


import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.databinding.ObservableBoolean;
import android.view.View;
import android.widget.Toast;

import com.github.volfor.notes.R;
import com.github.volfor.notes.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;

public class UserItemViewModel extends BaseObservable {

    public ObservableBoolean iAmAuthor = new ObservableBoolean(false);

    private DatabaseReference ref;
    private User user;

    public UserItemViewModel(Query ref, User user, String authorId) {
        this.ref = (DatabaseReference) ref;
        this.user = user;

        iAmAuthor.set(authorId.equals(FirebaseAuth.getInstance().getCurrentUser().getUid()));
    }

    public void onRemoveItemClick(View v) {
        ref.child(user.id).removeValue();
        Toast.makeText(v.getContext(), R.string.contributor_removed, Toast.LENGTH_SHORT).show();
    }

    @Bindable
    public String getName() {
        return user.name;
    }

    @Bindable
    public String getEmail() {
        return user.email;
    }

    @Bindable
    public String getAvatar() {
        return user.avatar;
    }

}
