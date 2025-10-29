package com.example.mha;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface HospitalDao {

    @Insert
    void insertHospital(HospitalEntity hospital);

    @Query("SELECT * FROM hospitals")
    List<HospitalEntity> getAllHospitals();

    @Query("SELECT * FROM hospitals WHERE hospitalId = :id LIMIT 1")
    HospitalEntity getHospitalById(int id);

    @Query("DELETE FROM hospitals")
    void deleteAll();
}