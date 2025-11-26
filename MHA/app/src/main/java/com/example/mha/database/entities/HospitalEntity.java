package com.example.mha.database.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "hospitals")
public class HospitalEntity {

    @PrimaryKey(autoGenerate = true)
    public int localId;   // Room-only ID

    public int hospitalID; // SERVER ID (must NOT auto-generate)

    public String name;
    public String city;
    public String postcode;
}
