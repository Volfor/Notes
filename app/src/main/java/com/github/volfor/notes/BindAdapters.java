package com.github.volfor.notes;

import android.databinding.BindingAdapter;
import android.net.Uri;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

public class BindAdapters {

    @BindingAdapter({"avatar"})
    public static void bindUserAvatar(ImageView view, Uri image) {
        Glide.with(view.getContext())
                .load(image)
                .error(R.drawable.user_avatar_placeholder)
                .into(view);
    }

}
