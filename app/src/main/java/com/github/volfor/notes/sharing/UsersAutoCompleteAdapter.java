package com.github.volfor.notes.sharing;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;

import com.github.volfor.notes.R;
import com.github.volfor.notes.databinding.ItemUserBinding;
import com.github.volfor.notes.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class UsersAutoCompleteAdapter extends BaseAdapter implements Filterable {

    private Context context;
    private List<User> users;
    private List<User> myResults;

    public UsersAutoCompleteAdapter(Context context) {
        this.context = context;

        myResults = new ArrayList<>();
        users = new ArrayList<>();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("users");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Map<String, Map<String, String>> usersMap = (Map<String, Map<String, String>>) dataSnapshot.getValue();

                for (Map.Entry<String, Map<String, String>> entry : usersMap.entrySet()) {
                    Map<String, String> userValue = entry.getValue();

                    User user = new User();
                    for (Map.Entry<String, String> userInfo : userValue.entrySet()) {
                        if (userInfo.getKey().equals("id")) {
                            user.id = userInfo.getValue();
                        }

                        if (userInfo.getKey().equals("name")) {
                            user.name = userInfo.getValue();
                        }

                        if (userInfo.getKey().equals("email")) {
                            user.email = userInfo.getValue();
                        }

                        if (userInfo.getKey().equals("avatar")) {
                            user.avatar = userInfo.getValue();
                        }
                    }

                    if (!user.id.equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                        users.add(user);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public int getCount() {
        return myResults.size();
    }

    @Override
    public User getItem(int i) {
        return myResults.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View v, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(context);

        ItemUserBinding binding = DataBindingUtil.inflate(inflater, R.layout.item_user, parent, false);
        binding.setUser(getItem(i));

        return binding.getRoot();
    }

    @Override
    public Filter getFilter() {
        Filter filter = new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults filterResults = new FilterResults();
                if (constraint != null) {
                    List<User> filtered = new ArrayList<>();
                    for (User user : users) {
//                        if (user.name.toLowerCase().startsWith(constraint.toString().toLowerCase())
//                                || user.email.toLowerCase().startsWith(constraint.toString().toLowerCase())) {
//                            filtered.add(user);
//                        }
                        if (user.name.toLowerCase().contains(constraint.toString().toLowerCase())
                                || user.email.toLowerCase().contains(constraint.toString().toLowerCase())) {
                            filtered.add(user);
                        }
                    }

                    filterResults.values = filtered;
                    filterResults.count = filtered.size();
                }
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                if (results != null && results.count > 0) {
                    myResults = (List<User>) results.values;
                    notifyDataSetChanged();
                } else {
                    notifyDataSetInvalidated();
                }
            }
        };

        return filter;
    }

}
