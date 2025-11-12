package com.example.mha.network;

public class RecordRequest {
    public int userID;
    public String allergies;
    public String medications;
    public String problems;

    public RecordRequest(int userID, String allergies, String medications, String problems) {
        this.userID = userID;
        this.allergies = allergies;
        this.medications = medications;
        this.problems = problems;
    }
}
