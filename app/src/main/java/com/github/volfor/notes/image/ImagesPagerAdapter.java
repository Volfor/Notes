package com.github.volfor.notes.image;

import android.databinding.DataBindingUtil;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.github.volfor.notes.R;
import com.github.volfor.notes.databinding.ItemFullscreenImageBinding;

import java.util.ArrayList;

public class ImagesPagerAdapter extends PagerAdapter {

    private ItemFullscreenImageBinding binding;

    private ArrayList<String> images;

    public ImagesPagerAdapter(ArrayList<String> images) {
        this.images = images;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        binding = DataBindingUtil.inflate(LayoutInflater.from(container.getContext()),
                R.layout.item_fullscreen_image, container, false);

        Glide.with(binding.imageFullscreen.getContext())
                .load(images.get(position))
                .placeholder(R.drawable.image_placeholder)
                .into(binding.imageFullscreen);

        container.addView(binding.getRoot());

        return binding.getRoot();
    }

    @Override
    public int getCount() {
        return images.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }

}
