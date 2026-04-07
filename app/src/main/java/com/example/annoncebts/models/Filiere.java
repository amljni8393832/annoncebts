package com.example.annoncebts.models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "filieres")
public class Filiere {
    @PrimaryKey(autoGenerate = true)
    private int idFiliere;
    private String nomFiliere;

    public Filiere() {}

    public int getIdFiliere() { return idFiliere; }
    public void setIdFiliere(int idFiliere) { this.idFiliere = idFiliere; }
    public String getNomFiliere() { return nomFiliere; }
    public void setNomFiliere(String nomFiliere) { this.nomFiliere = nomFiliere; }

    @Override
    public String toString() { return nomFiliere; }
}
