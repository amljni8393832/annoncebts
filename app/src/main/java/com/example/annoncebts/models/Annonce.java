package com.example.annoncebts.models;

import java.util.Date;
import java.util.List;

public class Annonce {
    private int idAnnonce;
    private String titre;
    private String description;
    private Date dateAnnonce;
    private String categorie;
    private StatutAnnonce statut;
    private Date dateCreation;
    private Date dateModification;
    private Date dateExpiration;

    private List<Filiere> filieres;
    private List<Niveau> niveaux;
    private Utilisateur createur;

    public Annonce() {}

    public void creer() {}
    public void publier() { this.statut = StatutAnnonce.PUBLIE; }
    public void modifier() { this.dateModification = new Date(); }
    public void supprimer() {}
    public void archiver() { this.statut = StatutAnnonce.ARCHIVE; }

    public int getIdAnnonce() { return idAnnonce; }
    public void setIdAnnonce(int idAnnonce) { this.idAnnonce = idAnnonce; }
    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Date getDateAnnonce() { return dateAnnonce; }
    public void setDateAnnonce(Date dateAnnonce) { this.dateAnnonce = dateAnnonce; }
    public String getCategorie() { return categorie; }
    public void setCategorie(String categorie) { this.categorie = categorie; }
    public StatutAnnonce getStatut() { return statut; }
    public void setStatut(StatutAnnonce statut) { this.statut = statut; }
    public Date getDateCreation() { return dateCreation; }
    public void setDateCreation(Date dateCreation) { this.dateCreation = dateCreation; }
    public Date getDateModification() { return dateModification; }
    public void setDateModification(Date dateModification) { this.dateModification = dateModification; }
    public Date getDateExpiration() { return dateExpiration; }
    public void setDateExpiration(Date dateExpiration) { this.dateExpiration = dateExpiration; }
    public List<Filiere> getFilieres() { return filieres; }
    public void setFilieres(List<Filiere> filieres) { this.filieres = filieres; }
    public List<Niveau> getNiveaux() { return niveaux; }
    public void setNiveaux(List<Niveau> niveaux) { this.niveaux = niveaux; }
    public Utilisateur getCreateur() { return createur; }
    public void setCreateur(Utilisateur createur) { this.createur = createur; }
}