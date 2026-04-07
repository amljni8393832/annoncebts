package com.example.annoncebts.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.annoncebts.R;
import com.example.annoncebts.models.AppDatabase;
import com.example.annoncebts.models.Role;
import com.example.annoncebts.models.Utilisateur;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.messaging.FirebaseMessaging;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";
    private EditText etEmail, etPassword;
    private AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        db = AppDatabase.getDatabase(this);
        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);

        MaterialButton btnRequestAccess = findViewById(R.id.btn_request_access);
        if (btnRequestAccess != null) btnRequestAccess.setOnClickListener(v -> navigateToRegister());

        Button btnLogin = findViewById(R.id.btn_login);
        if (btnLogin != null) btnLogin.setOnClickListener(v -> handleLogin());
    }

    private void handleLogin() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show();
            return;
        }

        new Thread(() -> {
            try {
                Utilisateur user = db.utilisateurDao().login(email, password);
                if (user != null && user.getRole() != null) {
                    // Subscribe to topics immediately upon login
                    subscribeToUserTopics(user);

                    SharedPreferences sharedPref = getSharedPreferences("UserSession", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putInt("userId", user.getIdUtilisateur());
                    editor.putString("userRole", user.getRole().name());
                    editor.apply();

                    runOnUiThread(() -> {
                        Intent intent;
                        if (user.getRole() == Role.ADMINISTRATEUR) {
                            intent = new Intent(LoginActivity.this, AdminHomeActivity.class);
                        } else {
                            intent = new Intent(LoginActivity.this, StudentHomeActivity.class);
                        }
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    });
                } else {
                    runOnUiThread(() -> Toast.makeText(LoginActivity.this, "Identifiants invalides", Toast.LENGTH_SHORT).show());
                }
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(LoginActivity.this, "Erreur de connexion", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void subscribeToUserTopics(Utilisateur user) {
        FirebaseMessaging fm = FirebaseMessaging.getInstance();
        
        // Role
        fm.subscribeToTopic("role_" + user.getRole().name());
        
        // Filieres
        if (user.getFiliere() != null) {
            for (String f : user.getFiliere().split(", ")) {
                fm.subscribeToTopic("filiere_" + f.replace(" ", "_"));
            }
        }
        
        // Niveaux
        if (user.getNiveau() != null) {
            for (String n : user.getNiveau().split(", ")) {
                fm.subscribeToTopic("niveau_" + n.replace(" ", "_").replace("è", "e"));
            }
        }
        Log.d(TAG, "Subscribed to topics for " + user.getEmail());
    }

    private void navigateToRegister() {
        startActivity(new Intent(this, RegisterActivity.class));
    }
}
