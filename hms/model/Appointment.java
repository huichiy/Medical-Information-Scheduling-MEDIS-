package hms.model;

/**
 * Appointment entity (Module 2).
 *
 * Association: an Appointment links one Patient and one Doctor
 *              (stored here as patientId / doctorId, with their names
 *               cached for display in the table).
 * ENCAPSULATION: all fields private, accessed via getters/setters.
 */
public class Appointment {

    private int appointmentId;
    private int patientId;
    private int doctorId;
    private String patientName; // for display only
    private String doctorName;  // for display only
    private String date;        // yyyy-MM-dd
    private String time;        // HH:mm:ss
    private String status;      // Scheduled / Completed / Cancelled

    /** Constructor for creating a NEW booking (id assigned by the database). */
    public Appointment(int patientId, int doctorId, String date, String time, String status) {
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.date = date;
        this.time = time;
        this.status = status;
    }

    /** Constructor for loading an EXISTING record from the database. */
    public Appointment(int appointmentId, int patientId, int doctorId,
                       String patientName, String doctorName,
                       String date, String time, String status) {
        this.appointmentId = appointmentId;
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.patientName = patientName;
        this.doctorName = doctorName;
        this.date = date;
        this.time = time;
        this.status = status;
    }

    public int getAppointmentId() {
        return appointmentId;
    }

    public void setAppointmentId(int appointmentId) {
        this.appointmentId = appointmentId;
    }

    public int getPatientId() {
        return patientId;
    }

    public void setPatientId(int patientId) {
        this.patientId = patientId;
    }

    public int getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(int doctorId) {
        this.doctorId = doctorId;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public String getDoctorName() {
        return doctorName;
    }

    public void setDoctorName(String doctorName) {
        this.doctorName = doctorName;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
