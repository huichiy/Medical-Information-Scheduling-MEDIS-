package com.medis.model;

public class Receptionist extends User {
    public Receptionist(int id, String username, String hash) {
        super(id, username, hash, "RECEPTIONIST");
    }

    @Override
    public String dashboardTitle() { return "Receptionist Dashboard"; }
}
