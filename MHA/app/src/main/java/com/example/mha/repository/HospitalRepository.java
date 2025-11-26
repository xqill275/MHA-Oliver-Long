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
        return hospitalDao.getById(id); // lookup by server hospitalID
    }

    // Insert hospital entity into Room and return the generated row id (synchronous)
    //    Call from a background thread.
    public long insert(HospitalEntity h) {
        return hospitalDao.insert(h);
    }


    // OFFLINE → ONLINE CONVERSION HELPERS

    public List<HospitalRequest> getAllHospitalsOffline() {

        List<HospitalEntity> entities = hospitalDao.getAll();
        List<HospitalRequest> list = new ArrayList<>();

        if (entities == null) return list;

        for (HospitalEntity h : entities) {

            HospitalRequest req =
                    new HospitalRequest(h.name, h.city, h.postcode);

            // restore server id if present
            req.hospitalID = h.hospitalID;
            list.add(req);
        }

        return list;
    }

    public HospitalRequest getHospitalRequestById(int id) {

        HospitalEntity h = hospitalDao.getById(id);
        if (h == null) return null;

        HospitalRequest req =
                new HospitalRequest(h.name, h.city, h.postcode);

        req.hospitalID = h.hospitalID;
        return req;
    }
}
