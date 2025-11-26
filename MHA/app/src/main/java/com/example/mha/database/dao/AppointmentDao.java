package com.example.mha.database.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import androidx.room.Transaction;

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

    @Update
    void update(AppointmentEntity appointment);

    @Query("DELETE FROM appointments")
    void clear();

    // Atomic update: set userID and status only if currently 'available'
    @Query("UPDATE appointments SET userID = :userId, status = 'booked' WHERE appointmentID = :appointmentId AND status = 'available'")
    int bookIfAvailable(int appointmentId, int userId);

    // Cancel appointment (set userID null and status available)
    @Query("UPDATE appointments SET userID = NULL, status = 'available' WHERE appointmentID = :appointmentId")
    int cancelById(int appointmentId);
}
