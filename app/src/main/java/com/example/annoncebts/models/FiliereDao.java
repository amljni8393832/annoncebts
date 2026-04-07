package com.example.annoncebts.models;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import java.util.List;

@Dao
public interface FiliereDao {
    @Query("SELECT * FROM filieres")
    List<Filiere> getAllFilieres();

    @Insert
    void insert(Filiere filiere);
}
