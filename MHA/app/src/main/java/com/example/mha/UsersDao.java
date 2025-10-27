package com.example.mha;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface UsersDao {

    @Insert
    void insert(UserEntity user);

    @Query("SELECT * FROM users")
    List<UserEntity> getAllUsers();

    @Query("SELECT * FROM users WHERE Email = :email LIMIT 1")
    UserEntity getUserByEmail(String email);

    @Query("DELETE FROM users")
    void deleteAll();

    // NEW: lookup using deterministic SHA-256 hashes
    @Query("SELECT * FROM users WHERE EmailHash = :emailHash AND NHSHash = :nhsHash AND DOBHash = :dobHash LIMIT 1")
    UserEntity getUserForLogin(String emailHash, String nhsHash, String dobHash);

    @Query("SELECT * FROM users WHERE uid = :userID")
    UserEntity getUserFromID(int userID);
}