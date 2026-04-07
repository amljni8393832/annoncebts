package com.example.annoncebts.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.example.annoncebts.database.AppDatabase;
import com.example.annoncebts.models.Role;
import com.example.annoncebts.models.Utilisateur;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class UtilisateurDao {
    private SQLiteDatabase db;

    public UtilisateurDao(SQLiteDatabase db) {
        this.db = db;
    }

    public long insert(Utilisateur utilisateur) {
        ContentValues values = new ContentValues();
        values.put(AppDatabase.KEY_UTILISATEUR_NOM, utilisateur.getNom());
        values.put(AppDatabase.KEY_UTILISATEUR_EMAIL, utilisateur.getEmail());
        values.put(AppDatabase.KEY_UTILISATEUR_PASSWORD, utilisateur.getMotDePasse());
        values.put(AppDatabase.KEY_UTILISATEUR_ROLE, utilisateur.getRole() != null ? utilisateur.getRole().name() : null);
        values.put(AppDatabase.KEY_UTILISATEUR_DATE_CREATION, utilisateur.getDateCreation() != null ? utilisateur.getDateCreation().getTime() : null);

        return db.insert(AppDatabase.TABLE_UTILISATEUR, null, values);
    }

    public int update(Utilisateur utilisateur) {
        ContentValues values = new ContentValues();
        values.put(AppDatabase.KEY_UTILISATEUR_NOM, utilisateur.getNom());
        values.put(AppDatabase.KEY_UTILISATEUR_EMAIL, utilisateur.getEmail());
        values.put(AppDatabase.KEY_UTILISATEUR_PASSWORD, utilisateur.getMotDePasse());
        values.put(AppDatabase.KEY_UTILISATEUR_ROLE, utilisateur.getRole() != null ? utilisateur.getRole().name() : null);

        return db.update(AppDatabase.TABLE_UTILISATEUR, values, AppDatabase.KEY_ID + " = ?",
                new String[]{String.valueOf(utilisateur.getIdUtilisateur())});
    }

    public void delete(int id) {
        db.delete(AppDatabase.TABLE_UTILISATEUR, AppDatabase.KEY_ID + " = ?",
                new String[]{String.valueOf(id)});
    }

    public List<Utilisateur> getAllUtilisateurs() {
        List<Utilisateur> utilisateurs = new ArrayList<>();
        Cursor c = db.query(AppDatabase.TABLE_UTILISATEUR, null, null, null, null, null, null);

        if (c.moveToFirst()) {
            do {
                utilisateurs.add(cursorToUtilisateur(c));
            } while (c.moveToNext());
        }
        c.close();
        return utilisateurs;
    }

    public Utilisateur getUtilisateurByEmail(String email) {
        Cursor c = db.query(AppDatabase.TABLE_UTILISATEUR, null, AppDatabase.KEY_UTILISATEUR_EMAIL + " = ?",
                new String[]{email}, null, null, null);
        if (c != null && c.moveToFirst()) {
            Utilisateur utilisateur = cursorToUtilisateur(c);
            c.close();
            return utilisateur;
        }
        return null;
    }

    public Utilisateur getUtilisateurById(int id) {
        Cursor c = db.query(AppDatabase.TABLE_UTILISATEUR, null, AppDatabase.KEY_ID + " = ?",
                new String[]{String.valueOf(id)}, null, null, null);
        if (c != null && c.moveToFirst()) {
            Utilisateur utilisateur = cursorToUtilisateur(c);
            c.close();
            return utilisateur;
        }
        return null;
    }

    private Utilisateur cursorToUtilisateur(Cursor c) {
        Utilisateur u = new Utilisateur();
        u.setIdUtilisateur(c.getInt(c.getColumnIndexOrThrow(AppDatabase.KEY_ID)));
        u.setNom(c.getString(c.getColumnIndexOrThrow(AppDatabase.KEY_UTILISATEUR_NOM)));
        u.setEmail(c.getString(c.getColumnIndexOrThrow(AppDatabase.KEY_UTILISATEUR_EMAIL)));
        u.setMotDePasse(c.getString(c.getColumnIndexOrThrow(AppDatabase.KEY_UTILISATEUR_PASSWORD)));
        
        String roleStr = c.getString(c.getColumnIndexOrThrow(AppDatabase.KEY_UTILISATEUR_ROLE));
        if (roleStr != null) u.setRole(Role.valueOf(roleStr));

        long creationMillis = c.getLong(c.getColumnIndexOrThrow(AppDatabase.KEY_UTILISATEUR_DATE_CREATION));
        if (creationMillis > 0) u.setDateCreation(new Date(creationMillis));

        return u;
    }
}