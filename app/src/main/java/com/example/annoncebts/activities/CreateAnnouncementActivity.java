package com.example.annoncebts.activities;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.annoncebts.R;
import com.example.annoncebts.models.AppDatabase;
import com.example.annoncebts.models.Filiere;
import com.example.annoncebts.models.Niveau;
import com.example.annoncebts.models.Notification;
import com.example.annoncebts.models.TypeNotification;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.json.JSONObject;

public class CreateAnnouncementActivity extends AppCompatActivity {
    private static final String TAG = "CreateAnnouncement";
    private static final String DB_URL = "https://annoncebtsv2-default-rtdb.europe-west1.firebasedatabase.app/";

    private EditText etTitle, etDescription, etExpirationDate;
    private TextView tvPageTitle;
    private ChipGroup cgFilieres, cgNiveaux, cgRoles;
    private Calendar expirationCalendar;
    private AppDatabase db;
    private boolean isEdit = false;
    private int notificationId = -1;
    private List<String> preSelectedRoles = new ArrayList<>();
    private List<String> preSelectedFilieres = new ArrayList<>();
    private List<String> preSelectedNiveaux = new ArrayList<>();
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_announcement);

        FirebaseApp.initializeApp(this);
        db = AppDatabase.getDatabase(this);
        
        try {
            mDatabase = FirebaseDatabase.getInstance(DB_URL).getReference("announcements");
        } catch (Exception e) {
            mDatabase = FirebaseDatabase.getInstance().getReference("announcements");
        }
        
        expirationCalendar = Calendar.getInstance();

        tvPageTitle = findViewById(R.id.tv_page_title);
        etTitle = findViewById(R.id.et_title);
        etDescription = findViewById(R.id.et_description);
        etExpirationDate = findViewById(R.id.et_expiration_date);
        cgFilieres = findViewById(R.id.cg_filieres);
        cgNiveaux = findViewById(R.id.cg_niveaux);
        cgRoles = findViewById(R.id.cg_roles);
        MaterialButton btnPublish = findViewById(R.id.btn_publish);

        findViewById(R.id.btn_close).setOnClickListener(v -> finish());
        etExpirationDate.setOnClickListener(v -> showDatePicker());

        isEdit = getIntent().getBooleanExtra("isEdit", false);
        if (isEdit) {
            notificationId = getIntent().getIntExtra("notificationId", -1);
            etTitle.setText(getIntent().getStringExtra("title"));
            etDescription.setText(getIntent().getStringExtra("description"));
            long exp = getIntent().getLongExtra("expiration", -1);
            if (exp != -1) {
                expirationCalendar.setTimeInMillis(exp);
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                etExpirationDate.setText(sdf.format(expirationCalendar.getTime()));
            }
            if (tvPageTitle != null) tvPageTitle.setText(R.string.edit_announcement_title);
            btnPublish.setText(R.string.update_announcement_button);
            parseAudienceFromMessage();
        } else {
            if (tvPageTitle != null) tvPageTitle.setText(R.string.new_announcement_title);
            btnPublish.setText(R.string.publish_button);
        }

        loadTargetChips();
        btnPublish.setOnClickListener(v -> handlePublish());
    }

    private void parseAudienceFromMessage() {
        new Thread(() -> {
            Notification n = db.notificationDao().getById(notificationId);
            if (n != null && n.getMessage() != null) {
                try {
                    String fullMsg = n.getMessage();
                    String audiencePart = fullMsg.split("\n\n")[0]; 
                    String[] segments = audiencePart.split("\\|");
                    for (String segment : segments) {
                        if (segment.contains("For:")) {
                            String rolesStr = segment.replace("For:", "").trim();
                            preSelectedRoles = Arrays.asList(rolesStr.split(", "));
                        } else if (segment.contains("Dept:")) {
                            String deptsStr = segment.replace("Dept:", "").trim();
                            preSelectedFilieres = Arrays.asList(deptsStr.split(", "));
                        } else if (segment.contains("Lvl:")) {
                            String lvlsStr = segment.replace("Lvl:", "").trim();
                            preSelectedNiveaux = Arrays.asList(lvlsStr.split(", "));
                        }
                    }
                } catch (Exception e) { e.printStackTrace(); }
            }
        }).start();
    }

    private void loadTargetChips() {
        new Thread(() -> {
            List<Filiere> filieres = db.filiereDao().getAllFilieres();
            List<Niveau> niveaux = db.niveauDao().getAllNiveaux();
            runOnUiThread(() -> {
                if (isEdit) {
                    for (int i = 0; i < cgRoles.getChildCount(); i++) {
                        Chip chip = (Chip) cgRoles.getChildAt(i);
                        chip.setChecked(preSelectedRoles.contains(chip.getText().toString()));
                    }
                }
                for (Filiere f : filieres) {
                    Chip chip = new Chip(this);
                    chip.setText(f.getNomFiliere());
                    chip.setCheckable(true);
                    if (isEdit && preSelectedFilieres.contains(f.getNomFiliere())) chip.setChecked(true);
                    cgFilieres.addView(chip);
                }
                for (Niveau n : niveaux) {
                    Chip chip = new Chip(this);
                    chip.setText(n.getLibelle());
                    chip.setCheckable(true);
                    if (isEdit && preSelectedNiveaux.contains(n.getLibelle())) chip.setChecked(true);
                    cgNiveaux.addView(chip);
                }
            });
        }).start();
    }

    private void showDatePicker() {
        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            expirationCalendar.set(Calendar.YEAR, year);
            expirationCalendar.set(Calendar.MONTH, month);
            expirationCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            etExpirationDate.setText(sdf.format(expirationCalendar.getTime()));
        }, expirationCalendar.get(Calendar.YEAR), expirationCalendar.get(Calendar.MONTH), expirationCalendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void handlePublish() {
        String title = etTitle.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        String dateStr = etExpirationDate.getText().toString().trim();

        List<String> selectedRoles = getSelectedChipText(cgRoles);
        List<String> selectedFilieres = getSelectedChipText(cgFilieres);
        List<String> selectedNiveaux = getSelectedChipText(cgNiveaux);

        if (title.isEmpty() || description.isEmpty() || dateStr.isEmpty() || 
            selectedRoles.isEmpty() || selectedFilieres.isEmpty() || selectedNiveaux.isEmpty()) {
            Toast.makeText(this, "Veuillez remplir tous les champs et sélectionner au moins une cible.", Toast.LENGTH_SHORT).show();
            return;
        }

        new Thread(() -> {
            Notification announcement;
            if (isEdit) {
                announcement = db.notificationDao().getById(notificationId);
            } else {
                announcement = new Notification();
                announcement.setDateEnvoi(new Date());
                String key = mDatabase.push().getKey();
                announcement.setFirebaseId(key);
            }

            if (announcement == null) return;
            
            String audienceInfo = String.format("For: %s | Dept: %s | Lvl: %s", 
                    String.join(", ", selectedRoles),
                    String.join(", ", selectedFilieres),
                    String.join(", ", selectedNiveaux));

            announcement.setMessage(audienceInfo + "\n\n" + title + "\n\n" + description);
            announcement.setType(TypeNotification.INFORMATION);
            
            expirationCalendar.set(Calendar.HOUR_OF_DAY, 23);
            expirationCalendar.set(Calendar.MINUTE, 59);
            expirationCalendar.set(Calendar.SECOND, 59);
            expirationCalendar.set(Calendar.MILLISECOND, 999);
            announcement.setDateExpiration(expirationCalendar.getTime());
            
            if (announcement.getFirebaseId() != null) {
                Map<String, Object> data = new HashMap<>();
                data.put("firebaseId", announcement.getFirebaseId());
                data.put("message", announcement.getMessage());
                data.put("type", announcement.getType().name());
                data.put("dateEnvoi", announcement.getDateEnvoi().getTime());
                data.put("dateExpiration", announcement.getDateExpiration().getTime());
                mDatabase.child(announcement.getFirebaseId()).setValue(data);
                
                // SEND TARGETED NOTIFICATION
                sendPushNotification(announcement, title, selectedRoles, selectedFilieres, selectedNiveaux);
            }
            
            if (isEdit) db.notificationDao().update(announcement);
            else db.notificationDao().insert(announcement);

            runOnUiThread(() -> {
                Toast.makeText(this, isEdit ? "Communication mise à jour !" : "Communication publiée !", Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            });
        }).start();
    }

    private void sendPushNotification(Notification n, String displayTitle, List<String> roles, List<String> filieres, List<String> niveaux) {
        new Thread(() -> {
            try {
                InputStream is = getAssets().open("service-account.json");
                StringBuilder sb = new StringBuilder();
                int i;
                while ((i = is.read()) != -1) { sb.append((char) i); }
                is.close();
                JSONObject serviceAccount = new JSONObject(sb.toString());
                String projectId = serviceAccount.getString("project_id");
                
                InputStream credStream = new java.io.ByteArrayInputStream(sb.toString().getBytes());
                GoogleCredentials credentials = GoogleCredentials.fromStream(credStream)
                        .createScoped(Arrays.asList("https://www.googleapis.com/auth/firebase.messaging"));
                credentials.refreshIfExpired();
                String accessToken = credentials.getAccessToken().getTokenValue();

                // BUILD SANITIZED CONDITION
                StringBuilder cond = new StringBuilder();
                
                // Roles
                cond.append("(");
                for (int j = 0; j < roles.size(); j++) {
                    String r = roles.get(j).equals("ÉTUDIANT") ? "ETUDIANT" : "ENSEIGNANT";
                    cond.append("'role_").append(r).append("' in topics");
                    if (j < roles.size() - 1) cond.append(" || ");
                }
                cond.append(") && (");

                // Filieres (Limit to first 3 to stay under 5 total topics limit)
                int limitF = Math.min(filieres.size(), 3);
                for (int j = 0; j < limitF; j++) {
                    cond.append("'filiere_").append(MainActivity.sanitizeTopic(filieres.get(j))).append("' in topics");
                    if (j < limitF - 1) cond.append(" || ");
                }
                cond.append(") && (");

                // Niveaux
                for (int j = 0; j < niveaux.size(); j++) {
                    cond.append("'niveau_").append(MainActivity.sanitizeTopic(niveaux.get(j))).append("' in topics");
                    if (j < niveaux.size() - 1) cond.append(" || ");
                }
                cond.append(")");

                Log.d(TAG, "Condition finale: " + cond.toString());

                JSONObject notification = new JSONObject();
                notification.put("title", "Nouvelle Annonce : " + displayTitle);
                notification.put("body", "Consultez la dernière communication académique.");

                JSONObject data = new JSONObject();
                data.put("firebaseId", n.getFirebaseId());
                data.put("fullMessage", n.getMessage());
                data.put("type", n.getType().name());
                data.put("dateEnvoi", String.valueOf(n.getDateEnvoi().getTime()));
                data.put("dateExpiration", String.valueOf(n.getDateExpiration().getTime()));

                JSONObject messageObj = new JSONObject();
                messageObj.put("condition", cond.toString());
                messageObj.put("notification", notification);
                messageObj.put("data", data); 
                
                JSONObject androidConfig = new JSONObject();
                androidConfig.put("priority", "high");
                JSONObject androidNotif = new JSONObject();
                androidNotif.put("channel_id", "announcements_channel");
                androidConfig.put("notification", androidNotif);
                messageObj.put("android", androidConfig);

                JSONObject root = new JSONObject();
                root.put("message", messageObj);

                OkHttpClient client = new OkHttpClient();
                RequestBody body = RequestBody.create(root.toString(), MediaType.get("application/json; charset=utf-8"));
                Request request = new Request.Builder()
                        .url("https://fcm.googleapis.com/v1/projects/" + projectId + "/messages:send")
                        .post(body)
                        .addHeader("Authorization", "Bearer " + accessToken)
                        .build();

                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) { Log.e(TAG, "Push FAILED", e); }
                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        Log.d(TAG, "Push Response Code: " + response.code());
                        if (response.code() != 200) Log.e(TAG, "Error: " + response.body().string());
                        response.close();
                    }
                });
            } catch (Exception e) { Log.e(TAG, "Error", e); }
        }).start();
    }

    private List<String> getSelectedChipText(ChipGroup chipGroup) {
        List<String> selected = new ArrayList<>();
        for (int i = 0; i < chipGroup.getChildCount(); i++) {
            Chip chip = (Chip) chipGroup.getChildAt(i);
            if (chip.isChecked()) selected.add(chip.getText().toString());
        }
        return selected;
    }
}
