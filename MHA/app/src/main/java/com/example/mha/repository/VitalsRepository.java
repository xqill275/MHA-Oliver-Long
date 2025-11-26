package com.example.mha.repository;

import android.content.Context;
import android.os.Build;

import com.example.mha.database.AppDatabase;
import com.example.mha.database.dao.VitalsDao;
import com.example.mha.database.entities.VitalEntity;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class VitalsRepository {
    private final VitalsDao vitalsDao;

    public VitalsRepository(Context context) {
        AppDatabase db = AppDatabase.getInstance(context);
        vitalsDao = db.vitalsDao();
    }

    // Mirror: GET /api/vitals/:userID -> latest
    public VitalEntity getLatestVitalsForUser(int userId) {
        return vitalsDao.getLatestForUser(userId);
    }

    // Mirror: POST /api/vitals/update
    // We simulate INSERT ... ON DUPLICATE KEY UPDATE by inserting a new row (or replacing)
    // For your original API, it looks like you keep only one row per user; to emulate that:
    public void upsertVitals(int userId, String temperature, String heartRate, String systolic, String diastolic) {
        String now = isoNow();

        // Option A: Insert new row (append history)
        VitalEntity v = new VitalEntity();
        v.userID = userId;
        v.temperature = temperature != null ? temperature : "";
        v.heartRate = heartRate != null ? heartRate : "";
        v.systolic = systolic != null ? systolic : "";
        v.diastolic = diastolic != null ? diastolic : "";
        v.timestamp = now;
        vitalsDao.insert(v);

        // Option B: If you prefer to keep only the latest, you can:
        // VitalEntity latest = vitalsDao.getLatestForUser(userId);
        // if (latest == null) { vitalsDao.insert(v); } else { latest.temperature=...; latest.timestamp=now; vitalsDao.update(latest); }
    }

    public List<VitalEntity> getAllVitalsForUser(int userId) {
        return vitalsDao.getVitalsForUser(userId);
    }

    private String isoNow() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return DateTimeFormatter.ISO_INSTANT.format(Instant.now().atOffset(ZoneOffset.UTC));
        }
        return "";
    }
}
