package com.example.mha.network;

public class AppointmentRequest {

    public int appointmentID;   // SERVER ID
    public int localId;         // ROOM ID (OFFLINE USE)
    public int hospitalID;

    public String appointmentDate;
    public String appointmentTime;
    public String status;

    public String hospitalName;
    public String hospitalCity;

    public AppointmentRequest(int hospitalID,
                              String appointmentDate,
                              String appointmentTime,
                              String status) {

        this.hospitalID = hospitalID;
        this.appointmentDate = appointmentDate;
        this.appointmentTime = appointmentTime;
        this.status = status;
    }
}

