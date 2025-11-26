package com.example.mha.repository;

import android.content.Context;

import com.example.mha.database.AppDatabase;
import com.example.mha.database.dao.HospitalDao;
import com.example.mha.database.entities.HospitalEntity;
import com.example.mha.network.HospitalRequest;

import java.util.ArrayList;
import java.util.List;

public class HospitalRepository {
    private final HospitalDao hospitalDao;

    public HospitalRepository(Context context) {
        AppDatabase db = AppDatabase.getInstance(context);
        hospitalDao = db.hospitalDao();
    }

    public List<HospitalEntity> getAll() {
        return hospitalDao.getAll();
    }

    public HospitalEntity getById(int id) {
        return hospitalDao.getById(id);
    }

    public long insert(HospitalEntity h) {
        return hospitalDao.insert(h);
    }

    private List<HospitalRequest> convertRoomHospitalsToRequests() {

        List<HospitalEntity> entities = this.getAll();
        List<HospitalRequest> list = new ArrayList<>();

        for (HospitalEntity h : entities) {

            HospitalRequest req =
                    new HospitalRequest(h.name, h.city, h.postcode);

            // ✅ RESTORE SERVER ID (THIS FIXES ID: 0)
            req.hospitalID = h.hospitalID;

            list.add(req);
        }

        return list;
    }

}
