package com.example.mha.database.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.mha.database.entities.VitalEntity;

import java.util.List;

@Dao
public interface VitalsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(VitalEntity v);

    @Update
    void update(VitalEntity v);

    @Query("SELECT * FROM vitals WHERE userID = :userId ORDER BY timestamp DESC")
    List<VitalEntity> getVitalsForUser(int userId);

    @Query("SELECT * FROM vitals WHERE userID = :userId ORDER BY timestamp DESC LIMIT 1")
    VitalEntity getLatestForUser(int userId);

    @Query("DELETE FROM vitals")
    void clear();
}
