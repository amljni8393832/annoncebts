package com.example.annoncebts.activities;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.example.annoncebts.R;
import com.example.annoncebts.models.AppDatabase;
import com.example.annoncebts.models.Role;
import com.example.annoncebts.models.SyncManager;
import com.example.annoncebts.models.Utilisateur;
import com.google.firebase.messaging.FirebaseMessaging;
import java.text.Normalizer;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (!isGranted) {
                    Toast.makeText(this, "Les notifications sont désactivées", Toast.LENGTH_LONG).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        createNotificationChannel();
        SyncManager.getInstance(this).startSync();
        updateNotificationSubscriptions();
        askNotificationPermission();

        new Handler(Looper.getMainLooper()).postDelayed(this::checkUserSession, 1500);
    }

    private void updateNotificationSubscriptions() {
        SharedPreferences sharedPref = getSharedPreferences("UserSession", Context.MODE_PRIVATE);
        int userId = sharedPref.getInt("userId", -1);

        if (userId != -1) {
            new Thread(() -> {
                AppDatabase db = AppDatabase.getDatabase(this);
                Utilisateur user = db.utilisateurDao().getUserById(userId);
                if (user != null) {
                    FirebaseMessaging fm = FirebaseMessaging.getInstance();
                    
                    // Unsubscribe from everything first to avoid old topic issues
                    fm.unsubscribeFromTopic("announcements");

                    // Role
                    fm.subscribeToTopic("role_" + user.getRole().name());

                    // Filieres
                    if (user.getFiliere() != null) {
                        for (String f : user.getFiliere().split(", ")) {
                            String topic = "filiere_" + sanitizeTopic(f);
                            fm.subscribeToTopic(topic);
                            Log.d(TAG, "Subscribed to Topic: " + topic);
                        }
                    }

                    // Niveaux
                    if (user.getNiveau() != null) {
                        for (String n : user.getNiveau().split(", ")) {
                            String topic = "niveau_" + sanitizeTopic(n);
                            fm.subscribeToTopic(topic);
                            Log.d(TAG, "Subscribed to Topic: " + topic);
                        }
                    }
                }
            }).start();
        }
    }

    public static String sanitizeTopic(String input) {
        if (input == null) return "";
        // Remove accents
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD);
        String result = normalized.replaceAll("[\\p{InCombiningDiacriticalMarks}]", "");
        // Replace everything not a letter or number with underscore
        return result.replaceAll("[^a-zA-Z0-9]", "_").toLowerCase();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("announcements_channel", 
                    "Annonces BTS", NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("Notifications académiques prioritaires");
            channel.enableLights(true);
            channel.enableVibration(true);
            NotificationManager nm = getSystemService(NotificationManager.class);
            if (nm != null) nm.createNotificationChannel(channel);
        }
    }

    private void askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
                    PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }
    }

    private void checkUserSession() {
        SharedPreferences sharedPref = getSharedPreferences("UserSession", Context.MODE_PRIVATE);
        int userId = sharedPref.getInt("userId", -1);
        String userRoleStr = sharedPref.getString("userRole", null);

        if (userId != -1 && userRoleStr != null) {
            Intent intent = Role.ADMINISTRATEUR.name().equals(userRoleStr) ?
                    new Intent(this, AdminHomeActivity.class) : new Intent(this, StudentHomeActivity.class);
            startActivity(intent);
        } else {
            startActivity(new Intent(this, LoginActivity.class));
        }
        finish();
    }
}
