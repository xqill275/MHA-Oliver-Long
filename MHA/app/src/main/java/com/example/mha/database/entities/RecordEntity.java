package com.example.mha.database.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "records")
public class RecordEntity {

    @PrimaryKey(autoGenerate = true)
    public int recordID;

    public int userID;
    public String allergies;
    public String medications;
    public String problems;
    public String updatedAt;
}
