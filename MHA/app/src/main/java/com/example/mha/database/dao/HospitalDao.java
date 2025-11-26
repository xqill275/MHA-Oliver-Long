package com.example.mha.database.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.mha.database.entities.HospitalEntity;

import java.util.List;

@Dao
public interface HospitalDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(HospitalEntity hospital);

    @Query("SELECT * FROM hospitals")
    List<HospitalEntity> getAll();

    // ✅ SERVER hospitalID lookup (NOT Room localId)
    @Query("SELECT * FROM hospitals WHERE hospitalID = :id")
    HospitalEntity getById(int id);

    @Query("DELETE FROM hospitals")
    void clear();
}
