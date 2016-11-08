package com.github.volfor.notes.note;

import android.databinding.DataBindingUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.volfor.notes.R;
import com.github.volfor.notes.Utils;
import com.github.volfor.notes.databinding.ItemImageBinding;

import java.util.List;

public class NoteImagesAdapter extends RecyclerView.Adapter<NoteImagesAdapter.ViewHolder> {

    private List<String> images;

    public NoteImagesAdapter(List<String> images) {
        this.images = images;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_image, parent, false);

        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.bindImage(images, position);
    }

    @Override
    public int getItemCount() {
        return images == null ? 0 : images.size();
    }

    public void changeList(List<String> newList) {
        this.images = newList;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        ItemImageBinding binding;

        public ViewHolder(View v) {
            super(v);
            binding = DataBindingUtil.bind(v);

            binding.image.setLayoutParams(Utils.getNoteImageParams(v.getContext()));
        }

        void bindImage(List<String> images, int position) {
            binding.setViewModel(new ImageItemViewModel(images, position));
        }
    }

}
