package com.example.annoncebts.activities;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EditProfileActivity extends AppCompatActivity {
    private EditText etName, etEmail, etPassword, etConfirmPassword;
    private ChipGroup cgFilieres, cgNiveaux;
    private TextView tvLabelFilieres, tvLabelNiveaux;
    private View divider;
    private AppDatabase db;
    private Utilisateur currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        db = AppDatabase.getDatabase(this);
        
        etName = findViewById(R.id.et_edit_name);
        etEmail = findViewById(R.id.et_edit_email);
        etPassword = findViewById(R.id.et_edit_password);
        etConfirmPassword = findViewById(R.id.et_edit_confirm_password);
        cgFilieres = findViewById(R.id.cg_edit_filieres);
        cgNiveaux = findViewById(R.id.cg_edit_niveaux);
        tvLabelFilieres = findViewById(R.id.tv_label_filieres);
        tvLabelNiveaux = findViewById(R.id.tv_label_niveaux);
        divider = findViewById(R.id.divider_profile);

        findViewById(R.id.toolbar).setOnClickListener(v -> finish());
        
        loadSpinnersAndData();

        MaterialButton btnUpdate = findViewById(R.id.btn_update_profile);
        btnUpdate.setOnClickListener(v -> handleUpdate());
    }

    private void loadSpinnersAndData() {
        SharedPreferences sharedPref = getSharedPreferences("UserSession", Context.MODE_PRIVATE);
        int userId = sharedPref.getInt("userId", -1);

        if (userId == -1) {
            finish();
            return;
        }

        new Thread(() -> {
            currentUser = db.utilisateurDao().getUserById(userId);
            List<Filiere> filieres = db.filiereDao().getAllFilieres();
            List<Niveau> niveaux = db.niveauDao().getAllNiveaux();

            if (currentUser != null) {
                runOnUiThread(() -> {
                    etName.setText(currentUser.getNom());
                    etEmail.setText(currentUser.getEmail());

                    if (currentUser.getRole() == Role.ADMINISTRATEUR) {
                        tvLabelFilieres.setVisibility(View.GONE);
                        cgFilieres.setVisibility(View.GONE);
                        tvLabelNiveaux.setVisibility(View.GONE);
                        cgNiveaux.setVisibility(View.GONE);
                        if (divider != null) divider.setVisibility(View.GONE);
                    } else {
                        setupChipGroup(cgFilieres, filieres, currentUser.getFiliere(), currentUser.getRole());
                        setupChipGroupNiveau(cgNiveaux, niveaux, currentUser.getNiveau(), currentUser.getRole());
                    }
                });
            }
        }).start();
    }

    private void setupChipGroup(ChipGroup group, List<Filiere> items, String selectedString, Role role) {
        group.removeAllViews();
        List<String> selectedList = selectedString != null ? Arrays.asList(selectedString.split(", ")) : new ArrayList<>();
        
        boolean singleSelection = (role == Role.ETUDIANT);
        group.setSingleSelection(singleSelection);
        group.setSelectionRequired(singleSelection);

        for (Filiere item : items) {
            Chip chip = (Chip) getLayoutInflater().inflate(R.layout.item_filter_chip, group, false);
            chip.setText(item.getNomFiliere());
            chip.setCheckable(true);
            if (selectedList.contains(item.getNomFiliere())) {
                chip.setChecked(true);
            }
            group.addView(chip);
        }
    }

    private void setupChipGroupNiveau(ChipGroup group, List<Niveau> items, String selectedString, Role role) {
        group.removeAllViews();
        List<String> selectedList = selectedString != null ? Arrays.asList(selectedString.split(", ")) : new ArrayList<>();
        
        boolean singleSelection = (role == Role.ETUDIANT);
        group.setSingleSelection(singleSelection);
        group.setSelectionRequired(singleSelection);

        for (Niveau item : items) {
            Chip chip = (Chip) getLayoutInflater().inflate(R.layout.item_filter_chip, group, false);
            chip.setText(item.getLibelle());
            chip.setCheckable(true);
            if (selectedList.contains(item.getLibelle())) {
                chip.setChecked(true);
            }
            group.addView(chip);
        }
    }

    private void handleUpdate() {
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        if (name.isEmpty() || email.isEmpty()) {
            Toast.makeText(this, "Le nom et l'email ne peuvent pas être vides", Toast.LENGTH_SHORT).show();
            return;
        }

        List<String> selectedFilieres = new ArrayList<>();
        List<String> selectedNiveaux = new ArrayList<>();

        if (currentUser.getRole() != Role.ADMINISTRATEUR) {
            selectedFilieres = getSelectedChips(cgFilieres);
            selectedNiveaux = getSelectedChips(cgNiveaux);

            if (selectedFilieres.isEmpty() || selectedNiveaux.isEmpty()) {
                Toast.makeText(this, "Veuillez sélectionner au moins une filière et un niveau", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        if (!password.isEmpty()) {
            if (password.length() < 8 || !password.matches(".*\\d.*")) {
                Toast.makeText(this, "Le mot de passe doit contenir au moins 8 caractères et un chiffre", Toast.LENGTH_LONG).show();
                return;
            }
            if (!password.equals(confirmPassword)) {
                Toast.makeText(this, "Les mots de passe ne correspondent pas", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        final List<String> finalFilieres = selectedFilieres;
        final List<String> finalNiveaux = selectedNiveaux;

        new Thread(() -> {
            if (!email.equals(currentUser.getEmail())) {
                Utilisateur existingUser = db.utilisateurDao().getUserByEmail(email);
                if (existingUser != null) {
                    runOnUiThread(() -> Toast.makeText(this, "Cet email est déjà utilisé", Toast.LENGTH_SHORT).show());
                    return;
                }
            }

            currentUser.setNom(name);
            currentUser.setEmail(email);
            if (!password.isEmpty()) {
                currentUser.setMotDePasse(password);
            }
            
            if (currentUser.getRole() != Role.ADMINISTRATEUR) {
                currentUser.setFiliere(String.join(", ", finalFilieres));
                currentUser.setNiveau(String.join(", ", finalNiveaux));
            }

            db.utilisateurDao().update(currentUser);
            
            runOnUiThread(() -> {
                Toast.makeText(this, "Profil mis à jour avec succès !", Toast.LENGTH_SHORT).show();
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
