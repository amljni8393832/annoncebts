package com.example.annoncebts.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.annoncebts.R;
import com.example.annoncebts.models.AppDatabase;
import com.example.annoncebts.models.Utilisateur;

public class ProfileActivity extends AppCompatActivity {
    private TextView tvName, tvEmail, tvRole;
    private AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        db = AppDatabase.getDatabase(this);

        tvName = findViewById(R.id.tv_profile_name);
        tvEmail = findViewById(R.id.tv_profile_email);
        tvRole = findViewById(R.id.tv_profile_role);

        loadUserProfile();

        findViewById(R.id.card_edit_profile).setOnClickListener(v -> {
            startActivity(new Intent(ProfileActivity.this, EditProfileActivity.class));
        });

        findViewById(R.id.btn_logout).setOnClickListener(v -> {
            SharedPreferences sharedPref = getSharedPreferences("UserSession", Context.MODE_PRIVATE);
            sharedPref.edit().clear().apply();
            startActivity(new Intent(ProfileActivity.this, LoginActivity.class));
            finish();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload data when returning from EditProfileActivity
        loadUserProfile();
    }

    private void loadUserProfile() {
        SharedPreferences sharedPref = getSharedPreferences("UserSession", Context.MODE_PRIVATE);
        int userId = sharedPref.getInt("userId", -1);

        if (userId == -1) {
            Toast.makeText(this, "Session expired", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        new Thread(() -> {
            Utilisateur user = db.utilisateurDao().getUserById(userId);
            if (user != null) {
                runOnUiThread(() -> {
                    tvName.setText(user.getNom());
                    tvEmail.setText(user.getEmail());
                    tvRole.setText(user.getRole().name());
                });
            }
        }).start();
    }
}
