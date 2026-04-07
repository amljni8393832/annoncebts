package com.example.annoncebts.models;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;
import androidx.core.app.NotificationCompat;
import com.example.annoncebts.R;
import com.example.annoncebts.activities.MainActivity;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    private static final String TAG = "MyFirebaseMsgService";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.d(TAG, "From: " + remoteMessage.getFrom());

        if (remoteMessage.getData().size() > 0) {
            Map<String, String> data = remoteMessage.getData();
            
            // PERFORM TARGETING CHECK HERE
            if (isUserTargeted(data)) {
                String title = data.get("title");
                String body = data.get("body");
                String firebaseId = data.get("firebaseId");

                if (firebaseId != null) {
                    saveNotificationToLocalDb(data);
                }

                if (title != null && body != null) {
                    sendNotification(title, body);
                }
            } else {
                Log.d(TAG, "Message received but user is not targeted. Skipping alert.");
            }
        }
    }

    private boolean isUserTargeted(Map<String, String> data) {
        SharedPreferences sharedPref = getSharedPreferences("UserSession", Context.MODE_PRIVATE);
        int userId = sharedPref.getInt("userId", -1);
        if (userId == -1) return false;

        // Get target criteria from the message
        String targetRoles = data.get("targetRoles");
        String targetFilieres = data.get("targetFilieres");
        String targetNiveaux = data.get("targetNiveaux");

        if (targetRoles == null || targetFilieres == null || targetNiveaux == null) return true; // Default to show if no info

        try {
            AppDatabase db = AppDatabase.getDatabase(this);
            Utilisateur user = db.utilisateurDao().getUserById(userId);
            if (user == null) return false;

            // 1. Check Role
            String userRole = user.getRole().name().equals("ETUDIANT") ? "ÉTUDIANT" : "ENSEIGNANT";
            if (!targetRoles.contains(userRole)) return false;

            // 2. Check Filiere (At least one match)
            boolean filiereMatch = false;
            List<String> targetsF = Arrays.asList(targetFilieres.split(", "));
            if (user.getFiliere() != null) {
                for (String f : user.getFiliere().split(", ")) {
                    if (targetsF.contains(f)) { filiereMatch = true; break; }
                }
            }
            if (!filiereMatch) return false;

            // 3. Check Niveau (At least one match)
            boolean niveauMatch = false;
            List<String> targetsN = Arrays.asList(targetNiveaux.split(", "));
            if (user.getNiveau() != null) {
                for (String n : user.getNiveau().split(", ")) {
                    if (targetsN.contains(n)) { niveauMatch = true; break; }
                }
            }
            return niveauMatch;

        } catch (Exception e) {
            Log.e(TAG, "Error during targeting check", e);
            return true; // Default to show on error to avoid missing announcements
        }
    }

    private void saveNotificationToLocalDb(Map<String, String> data) {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                AppDatabase db = AppDatabase.getDatabase(this);
                Notification n = new Notification();
                n.setFirebaseId(data.get("firebaseId"));
                n.setMessage(data.get("fullMessage"));
                n.setType(TypeNotification.valueOf(data.get("type")));
                
                String envoiStr = data.get("dateEnvoi");
                if (envoiStr != null) n.setDateEnvoi(new Date(Long.parseLong(envoiStr)));
                
                String expStr = data.get("dateExpiration");
                if (expStr != null) n.setDateExpiration(new Date(Long.parseLong(expStr)));

                Notification existing = db.notificationDao().getByFirebaseId(n.getFirebaseId());
                if (existing != null) {
                    n.setIdNotification(existing.getIdNotification());
                }
                db.notificationDao().insert(n);
                Log.d(TAG, "Notification saved to local DB from background push.");
            } catch (Exception e) {
                Log.e(TAG, "Error saving push to DB", e);
            }
        });
    }

    private void sendNotification(String title, String messageBody) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);

        String channelId = "announcements_channel";
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, channelId)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle(title)
                        .setContentText(messageBody)
                        .setAutoCancel(true)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId,
                    "Annonces BTS",
                    NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(channel);
        }

        notificationManager.notify(0, notificationBuilder.build());
    }
}
