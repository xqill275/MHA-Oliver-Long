package com.example.mha.network;

public class HospitalRequest {
    public String name;
    public String city;
    public String postcode;

    public HospitalRequest(String name, String city, String postcode) {
        this.name = name;
        this.city = city;
        this.postcode = postcode;
    }
}