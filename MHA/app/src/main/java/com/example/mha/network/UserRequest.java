package com.example.mha.network;

public class UserRequest {
    public String FullName;
    public String Email;
    public String PhoneNum;
    public String NHSnum;
    public String DateOfBirth;
    public String Role;
    public String EmailHash;
    public String NHSHash;
    public String DOBHash;

    public UserRequest(String fullName, String email, String phoneNum, String nhsNum,
                       String dateOfBirth, String role, String emailHash, String nhsHash, String dobHash) {
        this.FullName = fullName;
        this.Email = email;
        this.PhoneNum = phoneNum;
        this.NHSnum = nhsNum;
        this.DateOfBirth = dateOfBirth;
        this.Role = role;
        this.EmailHash = emailHash;
        this.NHSHash = nhsHash;
        this.DOBHash = dobHash;
    }
}