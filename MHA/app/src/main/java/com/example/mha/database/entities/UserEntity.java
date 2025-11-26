package com.example.mha.database.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "users")
public class UserEntity {

    @PrimaryKey
    public int UID;   // ✅ NOT auto-generated anymore

    public String FullName;
    public String Email;
    public String PhoneNum;
    public String NHSnum;
    public String DateOfBirth;
    public String Role;
    public String EmailHash;
    public String NHSHash;
    public String DOBHash;
}