package com.example.annoncebts.models;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import java.util.Date;

@Entity(tableName = "notifications", indices = {@Index(value = {"firebaseId"}, unique = true)})
public class Notification {
    @PrimaryKey(autoGenerate = true)
    private int idNotification;
    private String firebaseId; // Unique ID for syncing across devices
    private String message;
    private TypeNotification type;
    private Date dateEnvoi;
    private Date dateExpiration;
    
    private int idAnnonce;
    private int idUtilisateurDestinataire;

    public Notification() {}

    public void envoyer() {}

    // Getters and Setters
    public int getIdNotification() { return idNotification; }
    public void setIdNotification(int idNotification) { this.idNotification = idNotification; }
    public String getFirebaseId() { return firebaseId; }
    public void setFirebaseId(String firebaseId) { this.firebaseId = firebaseId; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public TypeNotification getType() { return type; }
    public void setType(TypeNotification type) { this.type = type; }
    public Date getDateEnvoi() { return dateEnvoi; }
    public void setDateEnvoi(Date dateEnvoi) { this.dateEnvoi = dateEnvoi; }
    public Date getDateExpiration() { return dateExpiration; }
    public void setDateExpiration(Date dateExpiration) { this.dateExpiration = dateExpiration; }
    public int getIdAnnonce() { return idAnnonce; }
    public void setIdAnnonce(int idAnnonce) { this.idAnnonce = idAnnonce; }
    public int getIdUtilisateurDestinataire() { return idUtilisateurDestinataire; }
    public void setIdUtilisateurDestinataire(int idUtilisateurDestinataire) { this.idUtilisateurDestinataire = idUtilisateurDestinataire; }
}
