package com.example.annoncebts.models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "niveaux")
public class Niveau {
    @PrimaryKey(autoGenerate = true)
    private int idNiveau;
    private String libelle;

    public Niveau() {}

    public int getIdNiveau() { return idNiveau; }
    public void setIdNiveau(int idNiveau) { this.idNiveau = idNiveau; }
    public String getLibelle() { return libelle; }
    public void setLibelle(String libelle) { this.libelle = libelle; }

    @Override
    public String toString() { return libelle; }
}
