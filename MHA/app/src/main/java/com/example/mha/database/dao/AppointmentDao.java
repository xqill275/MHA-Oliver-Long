package com.example.mha.database.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.mha.database.entities.AppointmentEntity;

import java.util.List;

@Dao
public interface AppointmentDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(AppointmentEntity appointment);

    @Query("SELECT * FROM appointments")
    List<AppointmentEntity> getAll();

    @Query("SELECT * FROM appointments WHERE userID = :userId")
    List<AppointmentEntity> getUserAppointments(int userId);

    @Query("SELECT * FROM appointments WHERE appointmentID = :appointmentId LIMIT 1")
    AppointmentEntity getById(int appointmentId);

    // BOOK BY *LOCAL ID* ONLY
    @Query("UPDATE appointments SET userID = :userId, status = 'booked' WHERE localId = :localId AND status = 'available'")
    int bookByLocalId(int localId, int userId);

    // CANCEL BY *LOCAL ID* ONLY
    @Query("UPDATE appointments SET userID = NULL, status = 'available' WHERE localId = :localId")
    int cancelByLocalId(int localId);

    @Query("SELECT * FROM appointments WHERE hospitalID = :hospitalId AND status = 'available'")
    List<AppointmentEntity> getAvailableByHospital(int hospitalId);

    @Query("SELECT * FROM appointments WHERE userID = :userId AND appointmentDate = :appointmentDate LIMIT 1")
    AppointmentEntity getByUserAndDate(int userId, String appointmentDate);
}

