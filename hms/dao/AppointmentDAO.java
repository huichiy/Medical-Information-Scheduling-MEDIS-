package hms.dao;

import hms.db.DatabaseConnection;
import hms.exception.DuplicateSlotException;
import hms.model.Appointment;
import hms.model.Doctor;
import hms.model.Patient;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for the Appointment module (Module 2).
 *
 * ABSTRACTION (clean architecture): the UI talks to these methods only and
 * never sees SQL or JDBC. All database details are hidden here.
 *
 * Uses PreparedStatement everywhere to prevent SQL injection.
 */
public class AppointmentDAO {

    /** Load all patients for the "Select Patient" dropdown. */
    public List<Patient> getAllPatients() throws SQLException {
        List<Patient> list = new ArrayList<>();
        String sql = "SELECT patient_id, name, age, gender, medical_history "
                   + "FROM patients ORDER BY name";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Patient(
                        rs.getInt("patient_id"),
                        rs.getString("name"),
                        rs.getInt("age"),
                        rs.getString("gender"),
                        rs.getString("medical_history")));
            }
        }
        return list;
    }

    /** Load all doctors for the "Select Doctor" dropdown. */
    public List<Doctor> getAllDoctors() throws SQLException {
        List<Doctor> list = new ArrayList<>();
        String sql = "SELECT doctor_id, name, specialization "
                   + "FROM doctors ORDER BY name";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Doctor(
                        rs.getInt("doctor_id"),
                        rs.getString("name"),
                        rs.getString("specialization")));
            }
        }
        return list;
    }

    /** Application-level check used to prevent duplicate time slots. */
    public boolean isSlotTaken(int doctorId, String date, String time) throws SQLException {
        String sql = "SELECT COUNT(*) FROM appointments "
                   + "WHERE doctor_id = ? AND appointment_date = ? AND appointment_time = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, doctorId);
            ps.setString(2, date);
            ps.setString(3, time);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    /**
     * Insert a new appointment.
     *
     * Duplicate time slots are prevented twice over:
     *   1. Application check via isSlotTaken() (friendly, early).
     *   2. Database backstop via the uq_doctor_slot UNIQUE constraint, which
     *      throws SQLIntegrityConstraintViolationException if two users book
     *      the same slot at the same moment.
     */
    public void bookAppointment(Appointment a) throws SQLException, DuplicateSlotException {
        if (isSlotTaken(a.getDoctorId(), a.getDate(), a.getTime())) {
            throw new DuplicateSlotException(
                    "This doctor is already booked on " + a.getDate()
                    + " at " + a.getTime() + ". Please choose another slot.");
        }

        String sql = "INSERT INTO appointments "
                   + "(patient_id, doctor_id, appointment_date, appointment_time, status) "
                   + "VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, a.getPatientId());
            ps.setInt(2, a.getDoctorId());
            ps.setString(3, a.getDate());
            ps.setString(4, a.getTime());
            ps.setString(5, a.getStatus());
            ps.executeUpdate();
        } catch (SQLIntegrityConstraintViolationException ex) {
            throw new DuplicateSlotException(
                    "This time slot was just taken. Please choose another slot.");
        }
    }

    /** Load every appointment with patient & doctor names for the table view. */
    public List<Appointment> getAllAppointments() throws SQLException {
        List<Appointment> list = new ArrayList<>();
        String sql =
                "SELECT a.appointment_id, a.patient_id, a.doctor_id, "
              + "       p.name AS patient_name, d.name AS doctor_name, "
              + "       a.appointment_date, a.appointment_time, a.status "
              + "FROM appointments a "
              + "JOIN patients p ON a.patient_id = p.patient_id "
              + "JOIN doctors  d ON a.doctor_id  = d.doctor_id "
              + "ORDER BY a.appointment_date, a.appointment_time";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Appointment(
                        rs.getInt("appointment_id"),
                        rs.getInt("patient_id"),
                        rs.getInt("doctor_id"),
                        rs.getString("patient_name"),
                        rs.getString("doctor_name"),
                        rs.getString("appointment_date"),
                        rs.getString("appointment_time"),
                        rs.getString("status")));
            }
        }
        return list;
    }
}
