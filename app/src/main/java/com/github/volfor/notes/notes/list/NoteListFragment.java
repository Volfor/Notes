package com.github.volfor.notes.notes.list;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.volfor.notes.R;
import com.github.volfor.notes.databinding.FragmentNoteListBinding;

public class NoteListFragment extends Fragment {

    private FragmentNoteListBinding binding;
    private NoteListViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_note_list, container, false);

        viewModel = new NoteListViewModel();
        binding.setViewModel(viewModel);

        return binding.getRoot();
    }

    @Override
    public void onDestroy() {
        viewModel.stop();
        super.onDestroy();
    }

}
