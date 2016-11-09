package com.github.volfor.notes.image;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.github.volfor.notes.R;
import com.github.volfor.notes.databinding.ActivityImageBinding;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Locale;

public class ImageActivity extends AppCompatActivity {

    private ActivityImageBinding binding;

    private String noteId;
    private ArrayList<String> images;
    private int position;
    private int currentPosition;

    private ActionBar supportActionBar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_image);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            noteId = extras.getString("noteId");
            position = extras.getInt("position");
            images = extras.getStringArrayList("images");
        }

        setSupportActionBar(binding.include.toolbar);

        supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setDisplayHomeAsUpEnabled(true);
        }

        setupViewPager(binding.imagesPager);
    }

    private void setupViewPager(final ViewPager pager) {
        setActionBarTitle(position);

        pager.setAdapter(new ImagesPagerAdapter(images));
        pager.setPageMargin(40);
        pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                setActionBarTitle(position);
                //todo delete image
                currentPosition = position;
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        pager.setCurrentItem(position);
    }

    private void setActionBarTitle(int position) {
        if (images.size() > 1) {
            supportActionBar.setTitle(String.format(Locale.getDefault(), "%d of %d", (position + 1), images.size()));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.image_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
            case R.id.delete_image:
                deleteImage(currentPosition);
                break;
            default:
        }

        return super.onOptionsItemSelected(item);
    }

    private void deleteImage(int position) {
        images.remove(position);

        FirebaseDatabase.getInstance()
                .getReference()
                .child("notes")
                .child(noteId)
                .child("images")
                .setValue(images);

        Toast.makeText(this, R.string.image_deleted, Toast.LENGTH_SHORT).show();
        finish();
    }

}
