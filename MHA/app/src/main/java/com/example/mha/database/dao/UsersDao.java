package com.example.mha.database.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.mha.database.entities.UserEntity;

import java.util.List;

@Dao
public interface UsersDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(UserEntity user);

    @Query("SELECT * FROM users WHERE UID = :id")
    UserEntity getUserById(int id);

    @Query("SELECT * FROM users")
    List<UserEntity> getAll();

    @Update
    void update(UserEntity user);

    @Query("UPDATE users SET Role = :role WHERE UID = :uid")
    void updateRole(int uid, String role);

    @Query("DELETE FROM users")
    void clear();


}
