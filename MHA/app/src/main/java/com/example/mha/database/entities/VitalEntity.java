package com.example.mha.database.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "vitals")
public class VitalEntity {

    @PrimaryKey(autoGenerate = true)
    public int vitalID;

    public int userID;
    public String temperature;
    public String heartRate;
    public String systolic;
    public String diastolic;
    public String timestamp;
}
