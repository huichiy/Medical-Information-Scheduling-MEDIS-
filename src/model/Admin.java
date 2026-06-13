package model;

public class Admin extends User {
    public Admin(int id, String username, String hash) {
        super(id, username, hash, "ADMIN");
    }

    @Override
    public String dashboardTitle() { return "Admin Dashboard"; }
}
