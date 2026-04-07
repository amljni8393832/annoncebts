package com.example.annoncebts.models;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import java.util.List;

@Dao
public interface UtilisateurDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Utilisateur utilisateur);

    @Update
    void update(Utilisateur utilisateur);

    @Query("SELECT * FROM utilisateurs WHERE email = :email LIMIT 1")
    Utilisateur getUserByEmail(String email);

    @Query("SELECT * FROM utilisateurs WHERE idUtilisateur = :id LIMIT 1")
    Utilisateur getUserById(int id);

    @Query("SELECT * FROM utilisateurs WHERE firebaseId = :fbId LIMIT 1")
    Utilisateur getUserByFirebaseId(String fbId);

    @Query("SELECT * FROM utilisateurs WHERE email = :email AND motDePasse = :password LIMIT 1")
    Utilisateur login(String email, String password);

    @Query("SELECT * FROM utilisateurs")
    List<Utilisateur> getAllUsers();
}
