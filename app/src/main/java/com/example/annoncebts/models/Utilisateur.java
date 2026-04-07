package com.example.annoncebts.models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.Index;
import java.util.Date;

@Entity(tableName = "utilisateurs", indices = {@Index(value = {"email"}, unique = true)})
public class Utilisateur {
    @PrimaryKey(autoGenerate = true)
    private int idUtilisateur;
    private String firebaseId;
    private String nom;
    private String email;
    private String motDePasse;
    private Role role;
    private String filiere;
    private String niveau;
    private Date dateCreation;

    public Utilisateur() {}

    // Getters and Setters
    public int getIdUtilisateur() { return idUtilisateur; }
    public void setIdUtilisateur(int idUtilisateur) { this.idUtilisateur = idUtilisateur; }
    public String getFirebaseId() { return firebaseId; }
    public void setFirebaseId(String firebaseId) { this.firebaseId = firebaseId; }
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getMotDePasse() { return motDePasse; }
    public void setMotDePasse(String motDePasse) { this.motDePasse = motDePasse; }
    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
    public String getFiliere() { return filiere; }
    public void setFiliere(String filiere) { this.filiere = filiere; }
    public String getNiveau() { return niveau; }
    public void setNiveau(String niveau) { this.niveau = niveau; }
    public Date getDateCreation() { return dateCreation; }
    public void setDateCreation(Date dateCreation) { this.dateCreation = dateCreation; }
}
