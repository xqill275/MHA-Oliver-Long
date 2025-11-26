package com.example.mha.database.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "appointments")
public class AppointmentEntity {

    @PrimaryKey(autoGenerate = true)
    public int localId;   // ROOM ONLY ID

    public int appointmentID;  // SERVER ID (NEVER autoGenerate)

    public int hospitalID;
    public Integer userID;
    public String appointmentDate;
    public String appointmentTime;
    public String status;
}


