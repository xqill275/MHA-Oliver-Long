package com.example.mha;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "users")
public class UserEntity {
    @PrimaryKey(autoGenerate = true)
    public int uid;

    @ColumnInfo(name = "FullName")
    public String fullName;

    @ColumnInfo(name = "Email")
    public String email;

    @ColumnInfo(name = "PhoneNum")
    public String phoneNum;

    @ColumnInfo(name = "NHSnum")
    public String NhsNum;

    @ColumnInfo(name = "DateOfBirth")
    public String DOB;

    @ColumnInfo(name = "Role")
    public String role;
}