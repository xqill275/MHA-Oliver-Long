package com.example.mha.network;

public class VitalsRequest {
    public int userID;
    public double temperature;
    public int heartRate;
    public int systolic;
    public int diastolic;

    public VitalsRequest(int userID, double temperature, int heartRate, int systolic, int diastolic) {
        this.userID = userID;
        this.temperature = temperature;
        this.heartRate = heartRate;
        this.systolic = systolic;
        this.diastolic = diastolic;
    }
}