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

    // Basic insert (admin / sync)
    public long insert(AppointmentEntity a) {
        return appointmentDao.insert(a);
    }

    // OFFLINE: add new appointment slot
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

        // offline placeholder for server id
        a.appointmentID = -1;

        return appointmentDao.insert(a);
    }

    // OFFLINE: book appointment
    // Accepts either server appointmentID or localId. Resolves to localId and updates by localId.
    public boolean bookAppointmentOffline(int appointmentIdOrLocalId, int userID) {

        AppointmentEntity entity = resolveToEntity(appointmentIdOrLocalId);
        if (entity == null) return false;

        int rows = appointmentDao.bookByLocalId(entity.localId, userID);
        return rows > 0;
    }

    // OFFLINE: cancel appointment
    public boolean cancelAppointmentOffline(int appointmentIdOrLocalId) {

        AppointmentEntity entity = resolveToEntity(appointmentIdOrLocalId);
        if (entity == null) return false;

        int rows = appointmentDao.cancelByLocalId(entity.localId);
        return rows > 0;
    }

    // Get available offline (for BookActivity spinner)
    public List<AppointmentRequest> getAvailableAppointmentsOffline(int hospitalId) {

        List<AppointmentEntity> entities =
                appointmentDao.getAvailableByHospital(hospitalId);

        return convertRoomAppointmentsToRequests(entities);
    }

    // Double booking prevention (offline)
    public boolean hasAppointmentForDate(int userId, String appointmentDate) {

        AppointmentEntity found =
                appointmentDao.getByUserAndDate(userId, appointmentDate);

        return found != null;
    }

    // Get user appointments (offline)
    public List<AppointmentRequest> getUserAppointmentsOffline(int userId) {

        List<AppointmentEntity> entities =
                appointmentDao.getUserAppointments(userId);

        return convertRoomAppointmentsToRequests(entities);
    }

    // Helper to resolve either server ID or localId -> AppointmentEntity
    private AppointmentEntity resolveToEntity(int appointmentIdOrLocalId) {
        // Try server ID lookup (DAO.getById uses appointmentID field)
        AppointmentEntity entity = appointmentDao.getById(appointmentIdOrLocalId);

        // If not found, fall back to localId scan
        if (entity == null) {
            List<AppointmentEntity> all = appointmentDao.getAll();
            if (all != null) {
                for (AppointmentEntity a : all) {
                    if (a.localId == appointmentIdOrLocalId) {
                        entity = a;
                        break;
                    }
                }
            }
        }
        return entity;
    }

    // ROOM -> API/UI conversion
    // IMPORTANT: fill both server appointmentID (if present) and localId so UI can use either.
    private List<AppointmentRequest> convertRoomAppointmentsToRequests(
            List<AppointmentEntity> entities
    ) {

        List<AppointmentRequest> list = new ArrayList<>();

        if (entities == null) return list;

        for (AppointmentEntity a : entities) {

            AppointmentRequest req =
                    new AppointmentRequest(
                            a.hospitalID,
                            a.appointmentDate,
                            a.appointmentTime,
                            a.status
                    );

            // always expose localId for offline operations
            req.localId = a.localId;

            // expose server appointmentID if present (> 0). Otherwise keep server ID negative and
            // UI should prefer localId for offline ops.
            if (a.appointmentID > 0) {
                req.appointmentID = a.appointmentID;
            } else {
                req.appointmentID = a.localId; // convenient fallback so older code that reads appointmentID still works
            }

            list.add(req);
        }

        return list;
    }

    public boolean bookAppointmentOfflineByAnyId(int appointmentIdOrLocalId, int userId) {

        AppointmentEntity entity = appointmentDao.getById(appointmentIdOrLocalId);

        if (entity == null) {
            List<AppointmentEntity> all = appointmentDao.getAll();
            for (AppointmentEntity a : all) {
                if (a.localId == appointmentIdOrLocalId) {
                    entity = a;
                    break;
                }
            }
        }

        if (entity == null) return false;

        return appointmentDao.bookByLocalId(entity.localId, userId) > 0;
    }


// OFFLINE: cancel by server ID OR local ID (USED BY RESCHEDULE)

    public boolean cancelAppointmentOfflineByAnyId(int appointmentIdOrLocalId) {

        AppointmentEntity entity = appointmentDao.getById(appointmentIdOrLocalId);

        if (entity == null) {
            List<AppointmentEntity> all = appointmentDao.getAll();
            if (all != null) {
                for (AppointmentEntity a : all) {
                    if (a.localId == appointmentIdOrLocalId) {
                        entity = a;
                        break;
                    }
                }
            }
        }

        if (entity == null) return false;

        return appointmentDao.cancelByLocalId(entity.localId) > 0;
    }
}
