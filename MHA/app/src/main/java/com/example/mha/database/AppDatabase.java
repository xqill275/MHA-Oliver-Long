package com.example.mha.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.mha.database.dao.AppointmentDao;
import com.example.mha.database.dao.HospitalDao;
import com.example.mha.database.dao.RecordsDao;
import com.example.mha.database.dao.UsersDao;
import com.example.mha.database.dao.VitalsDao;
import com.example.mha.database.entities.AppointmentEntity;
import com.example.mha.database.entities.HospitalEntity;
import com.example.mha.database.entities.RecordEntity;
import com.example.mha.database.entities.UserEntity;
import com.example.mha.database.entities.VitalEntity;

// Add these imports:
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(
        entities = {
                UserEntity.class,
                HospitalEntity.class,
                AppointmentEntity.class,
                RecordEntity.class,
                VitalEntity.class
        },
        version = 3,
        exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {

    // DAOs
    public abstract UsersDao usersDao();
    public abstract HospitalDao hospitalDao();
    public abstract AppointmentDao appointmentDao();
    public abstract RecordsDao recordDao();
    public abstract VitalsDao vitalsDao();

    // 🔥 ADD THIS EXECUTOR (used for background database writes)
    private static final int NUMBER_OF_THREADS = 4;
    public static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    private static volatile AppDatabase INSTANCE;

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    AppDatabase.class,
                                    "mha_local_database"
                            )
                            .fallbackToDestructiveMigration()
                            .allowMainThreadQueries()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
