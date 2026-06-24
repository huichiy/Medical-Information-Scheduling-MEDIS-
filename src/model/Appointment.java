package model;

import java.time.LocalDateTime;

public class Appointment {
    public enum Status { SCHEDULED, COMPLETED, CANCELLED }

    private int           appointmentId;
    private Patient       patient;
    private Doctor        doctor;
    private LocalDateTime dateTime;
    private Status        status;

    public Appointment(int id, Patient patient, Doctor doctor, LocalDateTime dt, Status status) {
        this.appointmentId = id;
        this.patient = patient;
        this.doctor  = doctor;
        this.dateTime = dt;
        this.status  = status;
    }

    /** Constructor for new appointments (defaults to SCHEDULED, id assigned by DB). */
    public Appointment(Patient patient, Doctor doctor, LocalDateTime dt) {
        this(0, patient, doctor, dt, Status.SCHEDULED);
    }

    public int           getAppointmentId() { return appointmentId; }
    public Patient       getPatient()       { return patient; }
    public Doctor        getDoctor()        { return doctor; }
    public LocalDateTime getDateTime()      { return dateTime; }
    public Status        getStatus()        { return status; }

    public void setAppointmentId(int id) { this.appointmentId = id; }
    public void setStatus(Status s)      { this.status = s; }
}
