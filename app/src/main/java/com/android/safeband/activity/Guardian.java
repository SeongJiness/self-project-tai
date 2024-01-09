package com.android.safeband.activity;

public class Guardian {
    private String name;
    private String phoneNumber;

    public Guardian(String name, String phoneNumber) {
        this.name = name;
        this.phoneNumber = phoneNumber;
    }

    public String getName() {
        return name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }
}
