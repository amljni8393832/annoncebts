package com.example.annoncebts.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.annoncebts.R;
import com.example.annoncebts.models.AppDatabase;
import com.example.annoncebts.models.Filiere;
import com.example.annoncebts.models.Niveau;
import com.example.annoncebts.models.Role;
import com.example.annoncebts.models.Utilisateur;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {
    private static final String TAG = "RegisterActivity";
    private static final String DB_URL = "https://annoncebtsv2-default-rtdb.europe-west1.firebasedatabase.app/";
    
    private EditText etName, etEmail, etPassword;
    private RadioGroup rgRole;
    private ChipGroup cgFilieres, cgNiveaux;
    private AppDatabase db;
    private DatabaseReference mUsersDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        db = AppDatabase.getDatabase(this);
        try {
            mUsersDatabase = FirebaseDatabase.getInstance(DB_URL).getReference("utilisateurs");
        } catch (Exception e) {
            mUsersDatabase = FirebaseDatabase.getInstance().getReference("utilisateurs");
        }

        etName = findViewById(R.id.et_name);
        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        rgRole = findViewById(R.id.rg_role);
        cgFilieres = findViewById(R.id.cg_filieres);
        cgNiveaux = findViewById(R.id.cg_niveaux);

        findViewById(R.id.btn_to_login).setOnClickListener(v -> finish());
        loadChips();

        MaterialButton btnRegister = findViewById(R.id.btn_register);
        btnRegister.setOnClickListener(v -> handleRegistration());

        rgRole.setOnCheckedChangeListener((group, checkedId) -> {
            boolean isStudent = (checkedId == R.id.rb_student);
            cgFilieres.setSelectionRequired(isStudent);
            cgFilieres.setSingleSelection(isStudent);
            cgNiveaux.setSelectionRequired(isStudent);
            cgNiveaux.setSingleSelection(isStudent);
        });
        
        cgFilieres.setSingleSelection(true);
        cgNiveaux.setSingleSelection(true);
    }

    private void loadChips() {
        new Thread(() -> {
            List<Filiere> filieres = db.filiereDao().getAllFilieres();
            List<Niveau> niveaux = db.niveauDao().getAllNiveaux();

            runOnUiThread(() -> {
                for (Filiere f : filieres) {
                    Chip chip = (Chip) getLayoutInflater().inflate(R.layout.item_filter_chip, cgFilieres, false);
                    chip.setText(f.getNomFiliere());
                    chip.setCheckable(true);
                    cgFilieres.addView(chip);
                }

                for (Niveau n : niveaux) {
                    Chip chip = (Chip) getLayoutInflater().inflate(R.layout.item_filter_chip, cgNiveaux, false);
                    chip.setText(n.getLibelle());
                    chip.setCheckable(true);
                    cgNiveaux.addView(chip);
                }
            });
        }).start();
    }

    private void handleRegistration() {
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        List<String> selectedFilieres = getSelectedChips(cgFilieres);
        List<String> selectedNiveaux = getSelectedChips(cgNiveaux);

        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedFilieres.isEmpty() || selectedNiveaux.isEmpty()) {
            Toast.makeText(this, "Veuillez sélectionner au moins un département et un niveau", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < 8 || !password.matches(".*\\d.*")) {
            Toast.makeText(this, "Le mot de passe doit contenir au moins 8 caractères et un chiffre", Toast.LENGTH_LONG).show();
            return;
        }

        new Thread(() -> {
            Utilisateur existingUser = db.utilisateurDao().getUserByEmail(email);
            if (existingUser != null) {
                runOnUiThread(() -> Toast.makeText(this, "Cet email est déjà utilisé", Toast.LENGTH_SHORT).show());
                return;
            }

            Role role = (rgRole.getCheckedRadioButtonId() == R.id.rb_student) ? Role.ETUDIANT : Role.ENSEIGNANT;

            Utilisateur newUser = new Utilisateur();
            String key = mUsersDatabase.push().getKey();
            newUser.setFirebaseId(key);
            newUser.setNom(name);
            newUser.setEmail(email);
            newUser.setMotDePasse(password);
            newUser.setRole(role);
            newUser.setFiliere(String.join(", ", selectedFilieres));
            newUser.setNiveau(String.join(", ", selectedNiveaux));
            newUser.setDateCreation(new Date());

            // 1. Sync to Cloud
            if (key != null) {
                Map<String, Object> userData = new HashMap<>();
                userData.put("firebaseId", key);
                userData.put("nom", newUser.getNom());
                userData.put("email", newUser.getEmail());
                userData.put("motDePasse", newUser.getMotDePasse());
                userData.put("role", newUser.getRole().name());
                userData.put("filiere", newUser.getFiliere());
                userData.put("niveau", newUser.getNiveau());
                userData.put("dateCreation", newUser.getDateCreation().getTime());

                mUsersDatabase.child(key).setValue(userData);
            }

            // 2. Save locally
            db.utilisateurDao().insert(newUser);

            runOnUiThread(() -> {
                Toast.makeText(this, "Inscription réussie !", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                finish();
            });
        }).start();
    }

    private List<String> getSelectedChips(ChipGroup group) {
        List<String> selected = new ArrayList<>();
        for (int i = 0; i < group.getChildCount(); i++) {
            Chip chip = (Chip) group.getChildAt(i);
            if (chip.isChecked()) {
                selected.add(chip.getText().toString());
            }
        }
        return selected;
    }
}
