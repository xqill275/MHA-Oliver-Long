package com.example.mha;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(
        entities = {
                UserEntity.class,
                HospitalEntity.class // ✅ added hospital entity
        },
        version = 2, // 🔼 bumped version because schema changed
        exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {

    public abstract UsersDao usersDao();
    public abstract HospitalDao hospitalDao(); // ✅ added hospital DAO

    private static volatile AppDatabase INSTANCE;

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "user-database")
                            .allowMainThreadQueries()
                            // ⚠️ temporarily enable destructive migration while developing
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}