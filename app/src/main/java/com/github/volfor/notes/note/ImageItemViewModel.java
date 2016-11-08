package com.github.volfor.notes.note;

import android.content.Intent;
import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.view.View;

import com.github.volfor.notes.image.ImageActivity;

import java.util.ArrayList;
import java.util.List;

public class ImageItemViewModel extends BaseObservable {

    private List<String> images;
    private int position;

    public ImageItemViewModel(List<String> images, int position) {
        this.images = images;
        this.position = position;
    }

    @Bindable
    public String getImage() {
        return images.get(position);
    }

    public void onItemClick(View v) {
        Intent intent = new Intent(v.getContext(), ImageActivity.class);
        intent.putStringArrayListExtra("images", (ArrayList<String>) images);
        intent.putExtra("position", position);
        v.getContext().startActivity(intent);
    }

}
