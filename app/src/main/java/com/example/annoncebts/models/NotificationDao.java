package com.example.annoncebts.models;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import java.util.List;

@Dao
public interface NotificationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Notification notification);

    @Update
    void update(Notification notification);

    @Delete
    void delete(Notification notification);

    @Query("DELETE FROM notifications WHERE firebaseId = :fbId")
    void deleteByFirebaseId(String fbId);

    @Query("SELECT * FROM notifications WHERE (dateExpiration IS NULL OR dateExpiration > :currentTime) ORDER BY dateEnvoi DESC")
    List<Notification> getActiveNotifications(long currentTime);

    @Query("SELECT * FROM notifications WHERE (dateExpiration IS NOT NULL AND dateExpiration <= :currentTime) ORDER BY dateEnvoi DESC")
    List<Notification> getArchivedNotifications(long currentTime);

    @Query("SELECT * FROM notifications WHERE idNotification = :id")
    Notification getById(int id);

    @Query("SELECT * FROM notifications WHERE firebaseId = :fbId LIMIT 1")
    Notification getByFirebaseId(String fbId);

    @Query("SELECT * FROM notifications ORDER BY dateEnvoi DESC")
    List<Notification> getAllNotifications();
}
