package com.example.mha;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface UsersDao {

    // Insert a new user
    @Insert
    void insert(UserEntity user);

    // Get all users
    @Query("SELECT * FROM users")
    List<UserEntity> getAllUsers();

    // Find a specific user by email
    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    UserEntity getUserByEmail(String email);

    // Delete all users (useful for debugging or reset)
    @Query("DELETE FROM users")
    void deleteAll();
}