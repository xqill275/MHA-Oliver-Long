package com.example.mha.network;

public class VitalsRequest {
    public int userID;
    public String temperature;
    public String heartRate;
    public String systolic;
    public String diastolic;

    public VitalsRequest(int userID, String temperature, String heartRate, String systolic, String diastolic) {
        this.userID = userID;
        this.temperature = temperature;
        this.heartRate = heartRate;
        this.systolic = systolic;
        this.diastolic = diastolic;
    }
}