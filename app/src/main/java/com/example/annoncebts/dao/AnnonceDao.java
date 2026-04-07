package com.example.annoncebts.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.example.annoncebts.database.AppDatabase;
import com.example.annoncebts.models.Annonce;
import com.example.annoncebts.models.StatutAnnonce;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class AnnonceDao {
    private SQLiteDatabase db;

    public AnnonceDao(SQLiteDatabase db) {
        this.db = db;
    }

    public long insert(Annonce annonce) {
        ContentValues values = new ContentValues();
        values.put(AppDatabase.KEY_ANNONCE_TITRE, annonce.getTitre());
        values.put(AppDatabase.KEY_ANNONCE_DESCRIPTION, annonce.getDescription());
        values.put(AppDatabase.KEY_ANNONCE_DATE, annonce.getDateAnnonce() != null ? annonce.getDateAnnonce().getTime() : null);
        values.put(AppDatabase.KEY_ANNONCE_CATEGORIE, annonce.getCategorie());
        values.put(AppDatabase.KEY_ANNONCE_STATUT, annonce.getStatut() != null ? annonce.getStatut().name() : null);
        values.put(AppDatabase.KEY_ANNONCE_DATE_CREATION, annonce.getDateCreation() != null ? annonce.getDateCreation().getTime() : null);
        values.put(AppDatabase.KEY_ANNONCE_DATE_MODIFICATION, annonce.getDateModification() != null ? annonce.getDateModification().getTime() : null);
        values.put(AppDatabase.KEY_ANNONCE_DATE_EXPIRATION, annonce.getDateExpiration() != null ? annonce.getDateExpiration().getTime() : null);

        return db.insert(AppDatabase.TABLE_ANNONCE, null, values);
    }

    public int update(Annonce annonce) {
        ContentValues values = new ContentValues();
        values.put(AppDatabase.KEY_ANNONCE_TITRE, annonce.getTitre());
        values.put(AppDatabase.KEY_ANNONCE_DESCRIPTION, annonce.getDescription());
        values.put(AppDatabase.KEY_ANNONCE_DATE, annonce.getDateAnnonce() != null ? annonce.getDateAnnonce().getTime() : null);
        values.put(AppDatabase.KEY_ANNONCE_CATEGORIE, annonce.getCategorie());
        values.put(AppDatabase.KEY_ANNONCE_STATUT, annonce.getStatut() != null ? annonce.getStatut().name() : null);
        values.put(AppDatabase.KEY_ANNONCE_DATE_MODIFICATION, new Date().getTime());
        values.put(AppDatabase.KEY_ANNONCE_DATE_EXPIRATION, annonce.getDateExpiration() != null ? annonce.getDateExpiration().getTime() : null);

        return db.update(AppDatabase.TABLE_ANNONCE, values, AppDatabase.KEY_ID + " = ?",
                new String[]{String.valueOf(annonce.getIdAnnonce())});
    }

    public void delete(int id) {
        db.delete(AppDatabase.TABLE_ANNONCE, AppDatabase.KEY_ID + " = ?",
                new String[]{String.valueOf(id)});
    }

    public List<Annonce> getAllAnnonces() {
        List<Annonce> annonces = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + AppDatabase.TABLE_ANNONCE;
        Cursor c = db.rawQuery(selectQuery, null);

        if (c.moveToFirst()) {
            do {
                annonces.add(cursorToAnnonce(c));
            } while (c.moveToNext());
        }
        c.close();
        return annonces;
    }

    public Annonce getAnnonceById(int id) {
        Cursor c = db.query(AppDatabase.TABLE_ANNONCE, null, AppDatabase.KEY_ID + " = ?",
                new String[]{String.valueOf(id)}, null, null, null);
        if (c != null && c.moveToFirst()) {
            Annonce annonce = cursorToAnnonce(c);
            c.close();
            return annonce;
        }
        return null;
    }

    private Annonce cursorToAnnonce(Cursor c) {
        Annonce a = new Annonce();
        a.setIdAnnonce(c.getInt(c.getColumnIndexOrThrow(AppDatabase.KEY_ID)));
        a.setTitre(c.getString(c.getColumnIndexOrThrow(AppDatabase.KEY_ANNONCE_TITRE)));
        a.setDescription(c.getString(c.getColumnIndexOrThrow(AppDatabase.KEY_ANNONCE_DESCRIPTION)));
        
        long dateMillis = c.getLong(c.getColumnIndexOrThrow(AppDatabase.KEY_ANNONCE_DATE));
        if (dateMillis > 0) a.setDateAnnonce(new Date(dateMillis));

        a.setCategorie(c.getString(c.getColumnIndexOrThrow(AppDatabase.KEY_ANNONCE_CATEGORIE)));
        
        String statutStr = c.getString(c.getColumnIndexOrThrow(AppDatabase.KEY_ANNONCE_STATUT));
        if (statutStr != null) a.setStatut(StatutAnnonce.valueOf(statutStr));

        long creationMillis = c.getLong(c.getColumnIndexOrThrow(AppDatabase.KEY_ANNONCE_DATE_CREATION));
        if (creationMillis > 0) a.setDateCreation(new Date(creationMillis));

        long modificationMillis = c.getLong(c.getColumnIndexOrThrow(AppDatabase.KEY_ANNONCE_DATE_MODIFICATION));
        if (modificationMillis > 0) a.setDateModification(new Date(modificationMillis));

        long expirationMillis = c.getLong(c.getColumnIndexOrThrow(AppDatabase.KEY_ANNONCE_DATE_EXPIRATION));
        if (expirationMillis > 0) a.setDateExpiration(new Date(expirationMillis));

        return a;
    }
}