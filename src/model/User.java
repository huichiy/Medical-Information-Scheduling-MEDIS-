package model;

public abstract class User {
    protected int    userId;
    protected String username;
    protected String passwordHash;
    protected String role;

    protected User(int userId, String username, String passwordHash, String role) {
        this.userId = userId;
        this.username = username;
        this.passwordHash = passwordHash;
        this.role = role;
    }

    public int    getUserId()       { return userId; }
    public String getUsername()     { return username; }
    public String getPasswordHash() { return passwordHash; }
    public String getRole()         { return role; }

    public abstract String dashboardTitle();
}
