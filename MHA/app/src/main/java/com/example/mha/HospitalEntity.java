package com.example.mha;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "hospitals")
public class HospitalEntity {
    @PrimaryKey(autoGenerate = true)
    public int hospitalID;

    @ColumnInfo(name = "name")
    public String name;

    @ColumnInfo(name = "city")
    public String city;

    @ColumnInfo(name = "postcode")
    public String postcode;
}
