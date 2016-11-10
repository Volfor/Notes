package com.github.volfor.notes;

import android.databinding.BindingAdapter;
import android.databinding.BindingConversion;
import android.net.Uri;
import android.support.annotation.ColorInt;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;

import com.bumptech.glide.Glide;

public class BindAdapters {

    @BindingConversion
    public static int convertBooleanToVisibility(boolean visible) {
        return visible ? View.VISIBLE : View.GONE;
    }

    @BindingAdapter({"avatar"})
    public static void bindUserAvatar(ImageView v, Uri image) {
        Glide.with(v.getContext())
                .load(image)
                .error(R.drawable.user_avatar_placeholder)
                .into(v);
    }

    @BindingAdapter({"avatar"})
    public static void bindUserAvatar(ImageView v, String image) {
        Glide.with(v.getContext())
                .load(image)
                .error(R.drawable.user_avatar_placeholder)
                .into(v);
    }

    @BindingAdapter({"adapter"})
    public static void bindAdapter(RecyclerView v, RecyclerView.Adapter adapter) {
        v.setHasFixedSize(true);
        v.setLayoutManager(new LinearLayoutManager(v.getContext()));
        v.setAdapter(adapter);

        adapter.notifyDataSetChanged();
    }

    @BindingAdapter({"noteImagesAdapter"})
    public static void bindNoteImagesAdapter(RecyclerView v, RecyclerView.Adapter adapter) {
        v.setHasFixedSize(true);
        v.setNestedScrollingEnabled(false);
        v.setLayoutManager(new GridLayoutManager(v.getContext(), 3) {
            @Override
            public boolean canScrollVertically() {
                return false;
            }
        });
        v.setAdapter(adapter);
    }

    @BindingAdapter({"onSeekBarChangedListener"})
    public static void bindOnSeekBarChangedListener(SeekBar v, SeekBar.OnSeekBarChangeListener listener) {
        v.setOnSeekBarChangeListener(listener);
    }

    @BindingAdapter({"imageUrl"})
    public static void bindImageUrl(ImageView v, String url) {
        Glide.with(v.getContext())
                .load(url)
                .placeholder(R.drawable.image_placeholder)
                .into(v);
    }

    @BindingAdapter({"toolbarColor"})
    public static void bindToolbarColor(Toolbar v, @ColorInt int color) {
        if (color == 0 || color == -1) {
            v.setBackgroundColor(v.getResources().getColor(R.color.colorPrimary));
        } else {
            v.setBackgroundColor(color);
        }
    }

}
