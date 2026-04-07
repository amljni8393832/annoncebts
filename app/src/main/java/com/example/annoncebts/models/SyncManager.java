package com.example.annoncebts.models;

import android.content.Context;
import android.util.Log;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;

public class SyncManager {
    private static final String TAG = "SyncManager";
    private static final String DB_URL = "https://annoncebtsv2-default-rtdb.europe-west1.firebasedatabase.app/";
    
    private static SyncManager instance;
    private final AppDatabase db;
    private final DatabaseReference mDatabase;
    private final DatabaseReference mUsersDatabase;
    private OnSyncCompleteListener listener;

    public interface OnSyncCompleteListener {
        void onSyncComplete();
    }

    private SyncManager(Context context) {
        db = AppDatabase.getDatabase(context);
        DatabaseReference tempRef;
        DatabaseReference tempUsersRef;
        try {
            tempRef = FirebaseDatabase.getInstance(DB_URL).getReference("announcements");
            tempUsersRef = FirebaseDatabase.getInstance(DB_URL).getReference("utilisateurs");
        } catch (Exception e) {
            Log.e(TAG, "Database init failed", e);
            tempRef = FirebaseDatabase.getInstance().getReference("announcements");
            tempUsersRef = FirebaseDatabase.getInstance().getReference("utilisateurs");
        }
        mDatabase = tempRef;
        mUsersDatabase = tempUsersRef;
    }

    public static synchronized SyncManager getInstance(Context context) {
        if (instance == null) {
            instance = new SyncManager(context.getApplicationContext());
        }
        return instance;
    }

    public void setOnSyncCompleteListener(OnSyncCompleteListener listener) {
        this.listener = listener;
    }

    public void startSync() {
        // Sync Announcements
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Executors.newSingleThreadExecutor().execute(() -> {
                    Set<String> firebaseIds = new HashSet<>();
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        try {
                            Map<String, Object> map = (Map<String, Object>) snapshot.getValue();
                            if (map != null) {
                                Notification n = new Notification();
                                String fbId = (String) map.get("firebaseId");
                                n.setFirebaseId(fbId);
                                firebaseIds.add(fbId);
                                n.setMessage((String) map.get("message"));
                                n.setType(TypeNotification.valueOf((String) map.get("type")));
                                Long envoiTime = (Long) map.get("dateEnvoi");
                                if (envoiTime != null) n.setDateEnvoi(new Date(envoiTime));
                                Long expTime = (Long) map.get("dateExpiration");
                                if (expTime != null) n.setDateExpiration(new Date(expTime));

                                if (fbId != null) {
                                    Notification localVersion = db.notificationDao().getByFirebaseId(fbId);
                                    if (localVersion != null) n.setIdNotification(localVersion.getIdNotification());
                                    db.notificationDao().insert(n);
                                }
                            }
                        } catch (Exception e) { Log.e(TAG, "Error parsing announcement", e); }
                    }
                    // Cleanup deleted local announcements
                    for (Notification local : db.notificationDao().getAllNotifications()) {
                        if (local.getFirebaseId() != null && !firebaseIds.contains(local.getFirebaseId())) {
                            db.notificationDao().delete(local);
                        }
                    }
                    if (listener != null) listener.onSyncComplete();
                });
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });

        // Sync Users
        mUsersDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Executors.newSingleThreadExecutor().execute(() -> {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        try {
                            Map<String, Object> map = (Map<String, Object>) snapshot.getValue();
                            if (map != null) {
                                Utilisateur u = new Utilisateur();
                                String fbId = (String) map.get("firebaseId");
                                u.setFirebaseId(fbId);
                                u.setNom((String) map.get("nom"));
                                u.setEmail((String) map.get("email"));
                                u.setMotDePasse((String) map.get("motDePasse"));
                                u.setRole(Role.valueOf((String) map.get("role")));
                                u.setFiliere((String) map.get("filiere"));
                                u.setNiveau((String) map.get("niveau"));
                                Long dateC = (Long) map.get("dateCreation");
                                if (dateC != null) u.setDateCreation(new Date(dateC));

                                if (fbId != null) {
                                    Utilisateur localUser = db.utilisateurDao().getUserByFirebaseId(fbId);
                                    if (localUser != null) u.setIdUtilisateur(localUser.getIdUtilisateur());
                                    db.utilisateurDao().insert(u);
                                }
                            }
                        } catch (Exception e) { Log.e(TAG, "Error parsing user", e); }
                    }
                });
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
    }
}
