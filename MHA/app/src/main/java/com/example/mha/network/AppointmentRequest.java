package com.example.mha.network;



public class AppointmentRequest {
    public int appointmentID;
    public int hospitalID;
    public String appointmentDate;
    public String appointmentTime;
    public String status;

    public AppointmentRequest(int hospitalID, String appointmentDate, String appointmentTime, String status) {
        this.hospitalID = hospitalID;
        this.appointmentDate = appointmentDate;
        this.appointmentTime = appointmentTime;
        this.status = status;
    }




}