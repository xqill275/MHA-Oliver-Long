package com.example.mha.database.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.mha.database.entities.RecordEntity;

@Dao
public interface RecordsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(RecordEntity record);

    @Update
    void update(RecordEntity record);

    @Query("SELECT * FROM records WHERE userID = :userId LIMIT 1")
    RecordEntity getByUser(int userId);

    @Query("DELETE FROM records")
    void clear();
}
