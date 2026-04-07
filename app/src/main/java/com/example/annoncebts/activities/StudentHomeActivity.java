package com.example.annoncebts.activities;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import com.example.annoncebts.R;
import com.example.annoncebts.fragments.ArchiveFragment;
import com.example.annoncebts.fragments.HomeFragment;
import com.example.annoncebts.fragments.ProfileFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class StudentHomeActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_home);

        BottomNavigationView nav = findViewById(R.id.bottom_navigation);
        
        // Load default fragment
        if (savedInstanceState == null) {
            replaceFragment(new HomeFragment());
        }
        
        nav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                replaceFragment(new HomeFragment());
                return true;
            } else if (id == R.id.nav_archive) {
                replaceFragment(new ArchiveFragment());
                return true;
            } else if (id == R.id.nav_profile) {
                replaceFragment(new ProfileFragment());
                return true;
            }
            return false;
        });
    }

    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, fragment);
        fragmentTransaction.commit();
    }
}
