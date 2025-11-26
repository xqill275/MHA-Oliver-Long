package com.example.mha.repository;

import android.content.Context;

import com.example.mha.database.AppDatabase;
import com.example.mha.database.dao.AppointmentDao;
import com.example.mha.database.entities.AppointmentEntity;
import com.example.mha.network.AppointmentRequest;

import java.util.ArrayList;
import java.util.List;

public class AppointmentRepository {

    private final AppointmentDao appointmentDao;

    public AppointmentRepository(Context context) {
        AppDatabase db = AppDatabase.getInstance(context);
        appointmentDao = db.appointmentDao();
    }

    // ✅ OFFLINE MIRROR: GET /api/appointments
    public List<AppointmentEntity> getAllAppointments() {
        return appointmentDao.getAll();
    }

    // ✅ OFFLINE MIRROR: POST /api/appointments/add
    public long addAppointmentSlotOffline(
            int hospitalID,
            String appointmentDate,
            String appointmentTime
    ) {

        AppointmentEntity a = new AppointmentEntity();
        a.hospitalID = hospitalID;
        a.userID = null;
        a.appointmentDate = appointmentDate;
        a.appointmentTime = appointmentTime;
        a.status = "available";

        a.appointmentID = -1; // ✅ OFFLINE TEMP SERVER ID

        return appointmentDao.insert(a);
    }

    // ✅ OFFLINE MIRROR: POST /api/appointments/book
    public boolean bookAppointmentOffline(int appointmentID, int userID) {
        int rows = appointmentDao.bookIfAvailable(appointmentID, userID);
        return rows > 0;
    }

    // ✅ OFFLINE MIRROR: POST /api/appointments/cancel
    public boolean cancelAppointmentOffline(int appointmentID) {
        int rows = appointmentDao.cancelById(appointmentID);
        return rows > 0;
    }

    public AppointmentEntity getByServerId(int id) {
        return appointmentDao.getById(id);
    }

    private List<AppointmentRequest> convertRoomAppointmentsToRequests(
            List<AppointmentEntity> entities
    ) {

        List<AppointmentRequest> list = new ArrayList<>();

        for (AppointmentEntity a : entities) {

            AppointmentRequest req =
                    new AppointmentRequest(
                            a.hospitalID,
                            a.appointmentDate,
                            a.appointmentTime,
                            a.status
                    );

            req.appointmentID = a.appointmentID; // ✅ CRITICAL FIX

            list.add(req);
        }

        return list;
    }
}

