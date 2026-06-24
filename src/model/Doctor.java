package model;

public class Doctor extends User {
    private int    doctorId;
    private String name;
    private String specialization;

    /** Use when Doctor is loaded as a user (with login account). */
    public Doctor(int userId, String username, String hash,
                  int doctorId, String name, String specialization) {
        super(userId, username, hash, "DOCTOR");
        this.doctorId = doctorId;
        this.name = name;
        this.specialization = specialization;
    }

    /** Use when Doctor is loaded as a pure entity (no user account). */
    public Doctor(int doctorId, String name, String specialization) {
        super(0, null, null, "DOCTOR");
        this.doctorId = doctorId;
        this.name = name;
        this.specialization = specialization;
    }

    public int    getDoctorId()       { return doctorId; }
    public String getName()           { return name; }
    public String getSpecialization() { return specialization; }
    public void   setName(String n)           { this.name = n; }
    public void   setSpecialization(String s) { this.specialization = s; }

    @Override public String dashboardTitle() { return "Doctor Dashboard"; }
    @Override public String toString()       { return name + " (" + specialization + ")"; }
}
