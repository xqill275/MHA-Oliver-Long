package com.example.mha.repository;

import android.content.Context;

import com.example.mha.database.AppDatabase;
import com.example.mha.database.dao.RecordsDao;
import com.example.mha.database.entities.RecordEntity;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

public class RecordRepository {
    private final RecordsDao recordsDao;

    public RecordRepository(Context context) {
        AppDatabase db = AppDatabase.getInstance(context);
        recordsDao = db.recordDao();
    }

    // Mirror: GET /api/records/:userID
    public RecordEntity getByUser(int userId) {
        return recordsDao.getByUser(userId);
    }

    // Mirror: POST /api/records/update
    // If a record exists for the user, update it; otherwise insert new one.
    // This simulates ON DUPLICATE KEY UPDATE.
    public void upsertRecord(int userId, String allergies, String medications, String problems) {
        RecordEntity existing = recordsDao.getByUser(userId);

        String now = isoNow();

        if (existing == null) {
            RecordEntity r = new RecordEntity();
            r.userID = userId;
            r.allergies = allergies != null ? allergies : "";
            r.medications = medications != null ? medications : "";
            r.problems = problems != null ? problems : "";
            r.updatedAt = now;
            recordsDao.insert(r);
        } else {
            existing.allergies = allergies != null ? allergies : "";
            existing.medications = medications != null ? medications : "";
            existing.problems = problems != null ? problems : "";
            existing.updatedAt = now;
            recordsDao.update(existing);
        }
    }

    private String isoNow() {
        // Java 8+ style ISO time. If using older, replace with SimpleDateFormat.
        return DateTimeFormatter.ISO_INSTANT.format(Instant.now().atOffset(ZoneOffset.UTC));
    }
}
