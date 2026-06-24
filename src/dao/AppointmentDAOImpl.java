package dao;

import db.DatabaseConnection;
import model.Appointment;
import model.Doctor;
import model.Patient;
import model.Result;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class AppointmentDAOImpl implements AppointmentDAO {
    /** Canonical storage format — keeps inserts, lookups and seed data consistent. */
    public static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final Connection conn = DatabaseConnection.getInstance().get();
    private final PatientDAO patientDAO = new PatientDAOImpl();
    private final DoctorDAO  doctorDAO  = new DoctorDAOImpl();

    @Override
    public Result insert(Appointment a) {
        String sql = "INSERT INTO appointments(patient_id, doctor_id, appointment_datetime, status) VALUES (?,?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, a.getPatient().getPatientId());
            ps.setInt(2, a.getDoctor().getDoctorId());
            ps.setString(3, a.getDateTime().format(FMT));
            ps.setString(4, a.getStatus().name());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) a.setAppointmentId(keys.getInt(1));
            }
            return Result.ok(a);
        } catch (SQLException e) {
            if (e.getMessage() != null && e.getMessage().toLowerCase().contains("unique"))
                return Result.fail("Time slot already taken");
            return Result.fail("Database error: " + e.getMessage());
        }
    }

    @Override
    public List<Appointment> findAll() {
        return query("SELECT * FROM appointments ORDER BY appointment_datetime", -1);
    }

    @Override
    public List<Appointment> findByDoctor(int doctorId) {
        return query("SELECT * FROM appointments WHERE doctor_id=? ORDER BY appointment_datetime", doctorId);
    }

    private List<Appointment> query(String sql, int doctorFilter) {
        List<Appointment> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            if (doctorFilter >= 0) ps.setInt(1, doctorFilter);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        } catch (SQLException e) {
            System.err.println("[AppointmentDAO.query] " + e.getMessage());
        }
        return list;
    }

    @Override
    public boolean existsByDoctorAndTime(int doctorId, LocalDateTime dt) {
        String sql = "SELECT COUNT(*) FROM appointments " +
                     "WHERE doctor_id=? AND appointment_datetime=? AND status='SCHEDULED'";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, doctorId);
            ps.setString(2, dt.format(FMT));
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("[AppointmentDAO.existsByDoctorAndTime] " + e.getMessage());
            return false;
        }
    }

    @Override
    public Result updateStatus(int appointmentId, Appointment.Status status) {
        try (PreparedStatement ps = conn.prepareStatement(
                "UPDATE appointments SET status=? WHERE appointment_id=?")) {
            ps.setString(1, status.name());
            ps.setInt(2, appointmentId);
            return ps.executeUpdate() == 1 ? Result.ok() : Result.fail("Appointment not found");
        } catch (SQLException e) {
            return Result.fail("Database error: " + e.getMessage());
        }
    }

    private Appointment map(ResultSet rs) throws SQLException {
        Patient p = patientDAO.findById(rs.getInt("patient_id")).orElse(null);
        Doctor  d = doctorDAO.findById(rs.getInt("doctor_id")).orElse(null);
        return new Appointment(
            rs.getInt("appointment_id"),
            p, d,
            LocalDateTime.parse(rs.getString("appointment_datetime"), FMT),
            Appointment.Status.valueOf(rs.getString("status"))
        );
    }
}
