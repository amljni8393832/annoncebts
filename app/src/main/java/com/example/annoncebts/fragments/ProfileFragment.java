package com.example.annoncebts.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.annoncebts.R;
import com.example.annoncebts.activities.EditProfileActivity;
import com.example.annoncebts.activities.LoginActivity;
import com.example.annoncebts.models.AppDatabase;
import com.example.annoncebts.models.Role;
import com.example.annoncebts.models.Utilisateur;

public class ProfileFragment extends Fragment {
    private TextView tvName, tvEmail, tvRole, tvFiliere, tvNiveau;
    private AppDatabase db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        db = AppDatabase.getDatabase(requireContext());

        tvName = view.findViewById(R.id.tv_profile_name);
        tvEmail = view.findViewById(R.id.tv_profile_email);
        tvRole = view.findViewById(R.id.tv_profile_role);
        tvFiliere = view.findViewById(R.id.tv_profile_filiere);
        tvNiveau = view.findViewById(R.id.tv_profile_niveau);

        loadUserProfile();

        view.findViewById(R.id.card_edit_profile).setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), EditProfileActivity.class));
        });

        view.findViewById(R.id.btn_logout).setOnClickListener(v -> {
            SharedPreferences sharedPref = getActivity().getSharedPreferences("UserSession", Context.MODE_PRIVATE);
            sharedPref.edit().clear().apply();
            startActivity(new Intent(getActivity(), LoginActivity.class));
            getActivity().finish();
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadUserProfile();
    }

    private void loadUserProfile() {
        SharedPreferences sharedPref = getActivity().getSharedPreferences("UserSession", Context.MODE_PRIVATE);
        int userId = sharedPref.getInt("userId", -1);

        if (userId == -1) {
            return;
        }

        new Thread(() -> {
            Utilisateur user = db.utilisateurDao().getUserById(userId);
            if (user != null && isAdded()) {
                requireActivity().runOnUiThread(() -> {
                    tvName.setText(user.getNom());
                    tvEmail.setText(user.getEmail());
                    
                    // Display correct role label
                    if (user.getRole() == Role.ADMINISTRATEUR) {
                        tvRole.setText("ADMIN");
                        tvFiliere.setVisibility(View.GONE);
                        tvNiveau.setVisibility(View.GONE);
                    } else {
                        tvRole.setText(user.getRole() == Role.ETUDIANT ? "STUDENT" : "TEACHER");
                        if (user.getRole() == Role.ETUDIANT) {
                            tvFiliere.setVisibility(View.VISIBLE);
                            tvNiveau.setVisibility(View.VISIBLE);
                            tvFiliere.setText(user.getFiliere());
                            tvNiveau.setText(user.getNiveau());
                        } else {
                            // Enseignant
                            tvFiliere.setVisibility(View.VISIBLE);
                            tvNiveau.setVisibility(View.VISIBLE);
                            tvFiliere.setText(user.getFiliere());
                            tvNiveau.setText(user.getNiveau());
                        }
                    }
                });
            }
        }).start();
    }
}
