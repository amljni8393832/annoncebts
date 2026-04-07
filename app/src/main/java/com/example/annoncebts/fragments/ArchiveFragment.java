package com.example.annoncebts.fragments;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.style.BackgroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import com.example.annoncebts.R;
import com.example.annoncebts.activities.AnnouncementDetailsActivity;
import com.example.annoncebts.models.AppDatabase;
import com.example.annoncebts.models.Filiere;
import com.example.annoncebts.models.Niveau;
import com.example.annoncebts.models.Notification;
import com.example.annoncebts.models.Role;
import com.example.annoncebts.models.Utilisateur;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ArchiveFragment extends Fragment {
    private LinearLayout containerArchive;
    private AppDatabase db;
    private List<Notification> allArchived = new ArrayList<>();
    private String currentSearchQuery = "";
    private Utilisateur currentUser;

    // Filter state
    private Calendar filterStartDate, filterEndDate;
    private List<String> filterFilieres = new ArrayList<>();
    private List<String> filterNiveaux = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_archive, container, false);
        containerArchive = view.findViewById(R.id.container_archive);
        EditText etSearch = view.findViewById(R.id.et_search_archive);
        db = AppDatabase.getDatabase(requireContext());

        if (etSearch != null) {
            etSearch.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    currentSearchQuery = s.toString().toLowerCase().trim();
                    filterAndDisplay();
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }

        View btnFilter = view.findViewById(R.id.btn_filter_archive);
        if (btnFilter != null) {
            btnFilter.setOnClickListener(v -> showFilterDialog());
        }
        
        loadUserDataAndArchives();
        
        return view;
    }

    private void showFilterDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_filters, null);
        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .create();

        EditText etStart = dialogView.findViewById(R.id.et_start_date);
        EditText etEnd = dialogView.findViewById(R.id.et_end_date);
        ChipGroup cgFiliere = dialogView.findViewById(R.id.cg_filter_filieres);
        ChipGroup cgNiveau = dialogView.findViewById(R.id.cg_filter_niveaux);
        View layoutDept = dialogView.findViewById(R.id.layout_dept_filter);
        View layoutLvl = dialogView.findViewById(R.id.layout_lvl_filter);

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        if (filterStartDate != null) etStart.setText(sdf.format(filterStartDate.getTime()));
        if (filterEndDate != null) etEnd.setText(sdf.format(filterEndDate.getTime()));

        etStart.setOnClickListener(v -> showDatePicker(etStart, true));
        etEnd.setOnClickListener(v -> showDatePicker(etEnd, false));

        if (currentUser != null && (currentUser.getRole() == Role.ADMINISTRATEUR || currentUser.getRole() == Role.ENSEIGNANT)) {
            if (layoutDept != null) layoutDept.setVisibility(View.VISIBLE);
            if (layoutLvl != null) layoutLvl.setVisibility(View.VISIBLE);
            loadFilterChips(cgFiliere, cgNiveau);
        }

        dialogView.findViewById(R.id.btn_apply_filters).setOnClickListener(v -> {
            filterFilieres = getSelectedChips(cgFiliere);
            filterNiveaux = getSelectedChips(cgNiveau);
            filterAndDisplay();
            dialog.dismiss();
        });

        dialogView.findViewById(R.id.btn_reset_filters).setOnClickListener(v -> {
            filterStartDate = null;
            filterEndDate = null;
            filterFilieres.clear();
            filterNiveaux.clear();
            filterAndDisplay();
            dialog.dismiss();
        });

        dialog.show();
    }

    private void showDatePicker(EditText et, boolean isStart) {
        Calendar cal = Calendar.getInstance();
        if (isStart && filterStartDate != null) cal = filterStartDate;
        if (!isStart && filterEndDate != null) cal = filterEndDate;

        new DatePickerDialog(requireContext(), (view, year, month, dayOfMonth) -> {
            Calendar selected = Calendar.getInstance();
            if (isStart) {
                selected.set(year, month, dayOfMonth, 0, 0, 0);
                selected.set(Calendar.MILLISECOND, 0);
                filterStartDate = selected;
            } else {
                selected.set(year, month, dayOfMonth, 23, 59, 59);
                selected.set(Calendar.MILLISECOND, 999);
                filterEndDate = selected;
            }
            
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            et.setText(sdf.format(selected.getTime()));
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void loadFilterChips(ChipGroup cgF, ChipGroup cgN) {
        new Thread(() -> {
            List<Filiere> filieres;
            List<Niveau> niveaux;
            
            if (currentUser.getRole() == Role.ADMINISTRATEUR) {
                filieres = db.filiereDao().getAllFilieres();
                niveaux = db.niveauDao().getAllNiveaux();
            } else {
                List<String> myDepts = Arrays.asList(currentUser.getFiliere().split(", "));
                List<String> myLvls = Arrays.asList(currentUser.getNiveau().split(", "));
                filieres = new ArrayList<>();
                for (String d : myDepts) { Filiere f = new Filiere(); f.setNomFiliere(d); filieres.add(f); }
                niveaux = new ArrayList<>();
                for (String l : myLvls) { Niveau n = new Niveau(); n.setLibelle(l); niveaux.add(n); }
            }

            if (isAdded()) {
                requireActivity().runOnUiThread(() -> {
                    if (cgF != null) {
                        for (Filiere f : filieres) {
                            Chip chip = (Chip) getLayoutInflater().inflate(R.layout.item_filter_chip, cgF, false);
                            chip.setText(f.getNomFiliere());
                            chip.setCheckable(true);
                            if (filterFilieres.contains(f.getNomFiliere())) chip.setChecked(true);
                            cgF.addView(chip);
                        }
                    }
                    if (cgN != null) {
                        for (Niveau n : niveaux) {
                            Chip chip = (Chip) getLayoutInflater().inflate(R.layout.item_filter_chip, cgN, false);
                            chip.setText(n.getLibelle());
                            chip.setCheckable(true);
                            if (filterNiveaux.contains(n.getLibelle())) chip.setChecked(true);
                            cgN.addView(chip);
                        }
                    }
                });
            }
        }).start();
    }

    private List<String> getSelectedChips(ChipGroup group) {
        List<String> selected = new ArrayList<>();
        if (group == null) return selected;
        for (int i = 0; i < group.getChildCount(); i++) {
            Chip chip = (Chip) group.getChildAt(i);
            if (chip.isChecked()) selected.add(chip.getText().toString());
        }
        return selected;
    }

    private void loadUserDataAndArchives() {
        SharedPreferences sharedPref = requireActivity().getSharedPreferences("UserSession", Context.MODE_PRIVATE);
        int userId = sharedPref.getInt("userId", -1);

        new Thread(() -> {
            currentUser = db.utilisateurDao().getUserById(userId);
            allArchived = db.notificationDao().getArchivedNotifications(new Date().getTime());
            if (isAdded()) {
                requireActivity().runOnUiThread(this::filterAndDisplay);
            }
        }).start();
    }

    private void filterAndDisplay() {
        containerArchive.removeAllViews();
        List<Notification> filteredList = new ArrayList<>();

        for (Notification n : allArchived) {
            boolean matchesSearch = currentSearchQuery.isEmpty() || n.getMessage().toLowerCase().contains(currentSearchQuery);
            boolean matchesDate = true;
            if (filterStartDate != null && n.getDateEnvoi().before(filterStartDate.getTime())) matchesDate = false;
            if (filterEndDate != null && n.getDateEnvoi().after(filterEndDate.getTime())) matchesDate = false;
            
            boolean matchesContent = true;
            if (!filterFilieres.isEmpty() || !filterNiveaux.isEmpty()) {
                matchesContent = checkContentMatch(n);
            }

            if (matchesSearch && matchesDate && matchesContent) {
                filteredList.add(n);
            }
        }

        if (filteredList.isEmpty()) {
            TextView tvNoData = new TextView(getContext());
            tvNoData.setText(getString(R.string.no_results));
            tvNoData.setPadding(0, 50, 0, 0);
            tvNoData.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            containerArchive.addView(tvNoData);
        } else {
            for (Notification a : filteredList) {
                addArchiveCard(a);
            }
        }
    }

    private boolean checkContentMatch(Notification n) {
        try {
            String audiencePart = n.getMessage().split("\n\n")[0];
            boolean deptMatch = filterFilieres.isEmpty();
            boolean lvlMatch = filterNiveaux.isEmpty();

            if (!deptMatch) {
                for (String f : filterFilieres) if (audiencePart.contains("Dept: " + f) || audiencePart.contains(", " + f)) deptMatch = true;
            }
            if (!lvlMatch) {
                for (String l : filterNiveaux) if (audiencePart.contains("Lvl: " + l) || audiencePart.contains(", " + l)) lvlMatch = true;
            }
            return deptMatch && lvlMatch;
        } catch (Exception e) { return true; }
    }

    private void addArchiveCard(Notification announcement) {
        View cardView = getLayoutInflater().inflate(R.layout.item_announcement_card, containerArchive, false);
        
        TextView tvAudience = cardView.findViewById(R.id.tv_audience_tag);
        TextView tvDate = cardView.findViewById(R.id.tv_date);
        TextView tvTitle = cardView.findViewById(R.id.tv_title);
        TextView tvDescription = cardView.findViewById(R.id.tv_description);
        View btnReadMore = cardView.findViewById(R.id.btn_read_more);

        tvAudience.setText("ARCHIVÉ");
        tvAudience.setBackgroundResource(R.drawable.bg_tag_gray); 
        tvAudience.setTextColor(getResources().getColor(R.color.primary, null));

        if(btnReadMore instanceof TextView) {
            ((TextView) btnReadMore).setText(R.string.read_more);
        }

        String message = announcement.getMessage();
        String tempTitle = "Communication";
        String tempDesc = message;

        try {
            String[] parts = message.split("\n\n");
            if (parts.length >= 3) {
                tempTitle = parts[1];
                tempDesc = parts[2];
            }
        } catch (Exception e) {}

        final String finalTitle = tempTitle;
        final String finalDesc = tempDesc;

        tvTitle.setText(getHighlightedText(finalTitle, currentSearchQuery));
        tvDescription.setText(getHighlightedText(finalDesc, currentSearchQuery));

        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy", Locale.FRENCH);
        tvDate.setText(sdf.format(announcement.getDateEnvoi()));

        View.OnClickListener listener = v -> {
            Intent intent = new Intent(getActivity(), AnnouncementDetailsActivity.class);
            intent.putExtra("notificationId", announcement.getIdNotification());
            intent.putExtra("title", finalTitle);
            intent.putExtra("description", finalDesc);
            if (announcement.getDateExpiration() != null) {
                intent.putExtra("expiration", announcement.getDateExpiration().getTime());
            }
            intent.putExtra("date", announcement.getDateEnvoi().getTime());
            startActivity(intent);
        };

        cardView.setOnClickListener(listener);
        btnReadMore.setOnClickListener(listener);
        containerArchive.addView(cardView);
    }

    private CharSequence getHighlightedText(String fullText, String query) {
        if (query.isEmpty() || !fullText.toLowerCase().contains(query)) {
            return fullText;
        }
        SpannableString spannable = new SpannableString(fullText);
        String lowerText = fullText.toLowerCase();
        int start = lowerText.indexOf(query);
        while (start >= 0) {
            int end = start + query.length();
            spannable.setSpan(new BackgroundColorSpan(Color.YELLOW), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            start = lowerText.indexOf(query, end);
        }
        return spannable;
    }
}
