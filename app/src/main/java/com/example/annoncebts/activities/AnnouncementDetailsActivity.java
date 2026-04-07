package com.example.annoncebts.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.example.annoncebts.R;
import com.example.annoncebts.models.AppDatabase;
import com.example.annoncebts.models.Notification;
import com.example.annoncebts.models.Role;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class AnnouncementDetailsActivity extends AppCompatActivity {
    private static final String TAG = "AnnouncementDetails";
    private static final String DB_URL = "https://annoncebtsv2-default-rtdb.europe-west1.firebasedatabase.app/";
    private static final int EDIT_REQUEST_CODE = 1001;
    
    private AppDatabase db;
    private int notificationId;
    private TextView tvTitle, tvDescription, tvExpiration, tvDate;
    private View adminActions;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_announcement_details);

        db = AppDatabase.getDatabase(this);
        notificationId = getIntent().getIntExtra("notificationId", -1);
        
        try {
            mDatabase = FirebaseDatabase.getInstance(DB_URL).getReference("announcements");
        } catch (Exception e) {
            mDatabase = FirebaseDatabase.getInstance().getReference("announcements");
        }

        tvTitle = findViewById(R.id.tv_detail_title);
        tvDescription = findViewById(R.id.tv_detail_description);
        tvExpiration = findViewById(R.id.tv_expiration_date);
        tvDate = findViewById(R.id.tv_detail_date);
        adminActions = findViewById(R.id.layout_admin_actions);

        Button btnEdit = findViewById(R.id.btn_edit);
        Button btnDelete = findViewById(R.id.btn_delete);
        
        if (btnEdit != null) btnEdit.setText(R.string.btn_edit);
        if (btnDelete != null) btnDelete.setText(R.string.btn_delete);

        loadAnnouncementData();

        SharedPreferences sharedPref = getSharedPreferences("UserSession", Context.MODE_PRIVATE);
        String userRole = sharedPref.getString("userRole", "");
        if (Role.ADMINISTRATEUR.name().equals(userRole)) {
            adminActions.setVisibility(View.VISIBLE);
        }

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        if (btnDelete != null) btnDelete.setOnClickListener(v -> showDeleteConfirmation());
        
        if (btnEdit != null) {
            btnEdit.setOnClickListener(v -> {
                new Thread(() -> {
                    Notification n = db.notificationDao().getById(notificationId);
                    if (n != null) {
                        runOnUiThread(() -> {
                            Intent intent = new Intent(this, CreateAnnouncementActivity.class);
                            intent.putExtra("isEdit", true);
                            intent.putExtra("notificationId", notificationId);
                            try {
                                String[] parts = n.getMessage().split("\n\n");
                                if (parts.length >= 3) {
                                    intent.putExtra("title", parts[1]);
                                    intent.putExtra("description", parts[2]);
                                } else {
                                    intent.putExtra("title", "");
                                    intent.putExtra("description", n.getMessage());
                                }
                            } catch (Exception e) {
                                intent.putExtra("title", "");
                                intent.putExtra("description", n.getMessage());
                            }
                            if (n.getDateExpiration() != null) {
                                intent.putExtra("expiration", n.getDateExpiration().getTime());
                            }
                            startActivityForResult(intent, EDIT_REQUEST_CODE);
                        });
                    }
                }).start();
            });
        }
    }

    private void loadAnnouncementData() {
        new Thread(() -> {
            Notification n = db.notificationDao().getById(notificationId);
            if (n != null) {
                runOnUiThread(() -> {
                    String[] parts = n.getMessage().split("\n\n");
                    if (parts.length >= 3) {
                        tvTitle.setText(parts[1]);
                        tvDescription.setText(parts[2]);
                    } else {
                        tvTitle.setText("Communication");
                        tvDescription.setText(n.getMessage());
                    }

                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.FRENCH);
                    if (n.getDateExpiration() != null) {
                        tvExpiration.setVisibility(View.VISIBLE);
                        tvExpiration.setText(getString(R.string.expires_on) + ": " + sdf.format(n.getDateExpiration()));
                    } else {
                        tvExpiration.setVisibility(View.GONE);
                    }
                    
                    tvDate.setText(getString(R.string.sent_on) + " " + new SimpleDateFormat("dd MMM yyyy", Locale.FRENCH).format(n.getDateEnvoi()));
                });
            }
        }).start();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == EDIT_REQUEST_CODE && resultCode == RESULT_OK) {
            loadAnnouncementData();
        }
    }

    private void showDeleteConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.delete_confirm_title)
                .setMessage(R.string.delete_confirm_msg)
                .setPositiveButton(R.string.btn_delete, (dialog, which) -> deleteAnnouncement())
                .setNegativeButton(R.string.cancel_button, null)
                .show();
    }

    private void deleteAnnouncement() {
        if (notificationId != -1) {
            new Thread(() -> {
                Notification n = db.notificationDao().getById(notificationId);
                if (n != null) {
                    // 1. Delete from Firebase
                    if (n.getFirebaseId() != null) {
                        mDatabase.child(n.getFirebaseId()).removeValue()
                                .addOnSuccessListener(aVoid -> Log.d(TAG, "Deleted from Firebase"))
                                .addOnFailureListener(e -> Log.e(TAG, "Failed to delete from Firebase", e));
                    }
                    
                    // 2. Delete from Local Room
                    db.notificationDao().delete(n);
                    
                    runOnUiThread(() -> {
                        Toast.makeText(this, R.string.delete_success, Toast.LENGTH_SHORT).show();
                        finish();
                    });
                }
            }).start();
        }
    }
}
