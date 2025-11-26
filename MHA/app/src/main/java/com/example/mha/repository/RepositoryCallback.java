package com.example.mha.repository;

public interface RepositoryCallback {
    void onSuccess();
    void onFailure(String error);
}