package com.example.mha.repository;

import android.content.Context;

import com.example.mha.database.AppDatabase;
import com.example.mha.database.dao.UsersDao;
import com.example.mha.database.entities.UserEntity;
import com.example.mha.network.ApiService;
import com.example.mha.network.NetworkChecker;
import com.example.mha.network.RetrofitClient;
import com.example.mha.network.UserRequest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserRepository {

    private final UsersDao usersDao;
    private final ApiService api;
    private final Context context;

    public UserRepository(Context context) {
        this.context = context.getApplicationContext();
        this.usersDao = AppDatabase.getInstance(this.context).usersDao();
        this.api = RetrofitClient.getClient().create(ApiService.class);
    }

    /**
     * ASYNC ONLINE/OFFLINE FETCH
     */
    public void getUsers(Callback<List<UserRequest>> callback) {

        // ONLINE → RETROFIT
        if (NetworkChecker.hasInternet(context)) {

            api.getUsers().enqueue(new Callback<List<UserRequest>>() {
                @Override
                public void onResponse(Call<List<UserRequest>> call, Response<List<UserRequest>> response) {

                    List<UserRequest> users = response.body();
                    if (users == null) return;

// 🔥 Fix: make a final copy to use inside lambda
                    final List<UserRequest> finalUsers = users;

                    AppDatabase.databaseWriteExecutor.execute(() -> {
                        usersDao.clear();

                        for (UserRequest req : finalUsers) {
                            UserEntity u = new UserEntity();
                            u.UID = req.UID;
                            u.FullName = req.FullName;
                            u.Email = req.Email;
                            u.PhoneNum = req.PhoneNum;
                            u.NHSnum = req.NHSnum;
                            u.DateOfBirth = req.DateOfBirth;
                            u.Role = req.Role;
                            u.EmailHash = req.EmailHash;
                            u.NHSHash = req.NHSHash;
                            u.DOBHash = req.DOBHash;

                            usersDao.insert(u);
                        }
                    });
                    callback.onResponse(call, Response.success(users));
                }

                @Override
                public void onFailure(Call<List<UserRequest>> call, Throwable t) {
                    // fallback to local room cache
                    callback.onResponse(call, Response.success(convertRoomToRequests()));
                }
            });

        } else {
            // OFFLINE: Room only
            callback.onResponse(null, Response.success(convertRoomToRequests()));
        }
    }

    /**
     * Convert Room → UserRequest including all hash fields
     */
    private List<UserRequest> convertRoomToRequests() {
        List<UserEntity> entities = usersDao.getAll();
        List<UserRequest> list = new ArrayList<>();

        for (UserEntity u : entities) {

            UserRequest req = new UserRequest(
                    u.FullName,
                    u.Email,
                    u.PhoneNum,
                    u.NHSnum,
                    u.DateOfBirth,
                    u.Role,
                    u.EmailHash,
                    u.NHSHash,
                    u.DOBHash
            );

            // ✅ CRITICAL: restore the REAL server UID for offline mode
            req.UID = u.UID;

            list.add(req);
        }

        return list;
    }

    public void registerUser(UserRequest req, RepositoryCallback callback) {

        if (NetworkChecker.hasInternet(context)) {
            // ONLINE → send to API
            api.addUser(req).enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (response.isSuccessful()) {

                        // Save to database as encrypted user
                        AppDatabase.databaseWriteExecutor.execute(() -> {
                            UserEntity u = new UserEntity();
                            u.FullName = req.FullName;
                            u.Email = req.Email;
                            u.PhoneNum = req.PhoneNum;
                            u.NHSnum = req.NHSnum;
                            u.DateOfBirth = req.DateOfBirth;
                            u.Role = req.Role;
                            u.EmailHash = req.EmailHash;
                            u.NHSHash = req.NHSHash;
                            u.DOBHash = req.DOBHash;

                            usersDao.insert(u);
                        });

                        callback.onSuccess();
                    } else {
                        callback.onFailure("Server error: " + response.code());
                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    callback.onFailure("Network error: " + t.getMessage());
                }
            });
        }
        else {
            // OFFLINE → only store locally
            AppDatabase.databaseWriteExecutor.execute(() -> {

                UserEntity u = new UserEntity();
                u.FullName = req.FullName;
                u.Email = req.Email;
                u.PhoneNum = req.PhoneNum;
                u.NHSnum = req.NHSnum;
                u.DateOfBirth = req.DateOfBirth;
                u.Role = req.Role;
                u.EmailHash = req.EmailHash;
                u.NHSHash = req.NHSHash;
                u.DOBHash = req.DOBHash;

                usersDao.insert(u);

                callback.onSuccess(); // treat as success
            });
        }
    }

    public interface RepositoryCallback {
        void onSuccess();
        void onFailure(String message);
    }

    public void updateUserRole(int uid, String encryptedRole, RepositoryCallback callback) {

        if (NetworkChecker.hasInternet(context)) {

            // ONLINE → API first
            Map<String, Object> body = new HashMap<>();
            body.put("uid", uid);
            body.put("role", encryptedRole);

            api.updateUserRole(body).enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {

                    if (response.isSuccessful()) {

                        // ✅ ALSO UPDATE ROOM
                        AppDatabase.databaseWriteExecutor.execute(() -> {
                            usersDao.updateRole(uid, encryptedRole);
                        });

                        callback.onSuccess();
                    } else {
                        callback.onFailure("Server error: " + response.code());
                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    callback.onFailure("Network error: " + t.getMessage());
                }
            });

        } else {

            // ✅ OFFLINE → ROOM ONLY
            AppDatabase.databaseWriteExecutor.execute(() -> {
                usersDao.updateRole(uid, encryptedRole);
                callback.onSuccess(); // treat as success offline
            });
        }
    }
}
