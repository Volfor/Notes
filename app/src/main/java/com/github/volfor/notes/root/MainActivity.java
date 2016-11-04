package com.github.volfor.notes.root;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;

import com.github.volfor.notes.R;
import com.github.volfor.notes.databinding.ActivityMainBinding;
import com.github.volfor.notes.databinding.DrawerHeaderBinding;
import com.github.volfor.notes.login.LoginActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private ActionBarDrawerToggle actionBarDrawerToggle;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user == null) {
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
        }

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        setSupportActionBar(binding.include.toolbar);

        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setHomeAsUpIndicator(R.drawable.ic_menu_white_24dp);
            supportActionBar.setDisplayHomeAsUpEnabled(true);
        }

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, new NoteListFragment())
                    .commit();
        }

        DrawerHeaderBinding headerBinding = DataBindingUtil.inflate(getLayoutInflater(),
                R.layout.drawer_header, binding.drawerLayout, false);
        headerBinding.setUser(user);

        binding.navView.addHeaderView(headerBinding.getRoot());

        setupDrawer();
    }

    private void setupDrawer() {
        binding.navView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.nav_my_notes:
                        if (!(getSupportFragmentManager().findFragmentById(R.id.container) instanceof NoteListFragment)) {
                            getSupportFragmentManager().beginTransaction()
                                    .replace(R.id.container, new NoteListFragment())
                                    .commit();
                        }

                        menuItem.setChecked(true);
                        break;
                    case R.id.nav_contrib:
                        if (!(getSupportFragmentManager().findFragmentById(R.id.container) instanceof ContributionsFragment)) {
                            getSupportFragmentManager().beginTransaction()
                                    .replace(R.id.container, new ContributionsFragment())
                                    .commit();
                        }

                        menuItem.setChecked(true);
                        break;
                    case R.id.nav_logout:
                        FirebaseAuth.getInstance().signOut();

                        startActivity(new Intent(MainActivity.this, LoginActivity.class));
                        finish();

                        break;
                    default:
                }

                binding.drawerLayout.closeDrawers();

                return true;
            }
        });

        actionBarDrawerToggle = new ActionBarDrawerToggle(this, binding.drawerLayout,
                binding.include.toolbar, R.string.drawer_open, R.string.drawer_close) {

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }
        };

        binding.drawerLayout.addDrawerListener(actionBarDrawerToggle);

        actionBarDrawerToggle.syncState();
    }

    @Override
    public void onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawers();
            return;
        }

        super.onBackPressed();
    }

}
