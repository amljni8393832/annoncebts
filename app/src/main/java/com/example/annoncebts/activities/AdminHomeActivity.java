package com.example.annoncebts.activities;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import com.example.annoncebts.R;
import com.example.annoncebts.fragments.AdminHomeFragment;
import com.example.annoncebts.fragments.ArchiveFragment;
import com.example.annoncebts.fragments.ProfileFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

public class AdminHomeActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_home);

        BottomNavigationView nav = findViewById(R.id.bottom_navigation);
        ExtendedFloatingActionButton fabAdd = findViewById(R.id.fab_add_announcement);

        // Load default fragment
        if (savedInstanceState == null) {
            replaceFragment(new AdminHomeFragment());
        }

        fabAdd.setOnClickListener(v -> {
            startActivity(new Intent(this, CreateAnnouncementActivity.class));
        });

        nav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                replaceFragment(new AdminHomeFragment());
                fabAdd.show();
                return true;
            } else if (id == R.id.nav_archive) {
                replaceFragment(new ArchiveFragment());
                fabAdd.hide();
                return true;
            } else if (id == R.id.nav_profile) {
                replaceFragment(new ProfileFragment());
                fabAdd.hide();
                return true;
            }
            return false;
        });
    }

    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.admin_fragment_container, fragment);
        fragmentTransaction.commit();
    }
}
