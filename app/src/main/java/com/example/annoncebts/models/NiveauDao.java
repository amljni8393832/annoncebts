package com.example.annoncebts.models;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import java.util.List;

@Dao
public interface NiveauDao {
    @Query("SELECT * FROM niveaux")
    List<Niveau> getAllNiveaux();

    @Insert
    void insert(Niveau niveau);
}
