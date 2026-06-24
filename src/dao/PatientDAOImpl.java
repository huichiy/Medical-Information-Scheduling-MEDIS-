package dao;

import db.DatabaseConnection;
import model.Patient;
import model.Result;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PatientDAOImpl implements PatientDAO {
    private final Connection conn = DatabaseConnection.getInstance().get();

    @Override
    public Result insert(Patient p) {
        String sql = "INSERT INTO patients(name, age, gender, medical_history) VALUES (?,?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, p.getName());
            ps.setInt(2, p.getAge());
            ps.setString(3, p.getGender());
            ps.setString(4, p.getMedicalHistory());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) p.setPatientId(keys.getInt(1));
            }
            return Result.ok(p);
        } catch (SQLException e) {
            return Result.fail("Database error: " + e.getMessage());
        }
    }

    @Override
    public Optional<Patient> findById(int id) {
        try (PreparedStatement ps = conn.prepareStatement("SELECT * FROM patients WHERE patient_id=?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(map(rs));
            }
        } catch (SQLException e) {
            System.err.println("[PatientDAO.findById] " + e.getMessage());
        }
        return Optional.empty();
    }

    @Override
    public List<Patient> findAll() {
        List<Patient> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement("SELECT * FROM patients ORDER BY patient_id");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) {
            System.err.println("[PatientDAO.findAll] " + e.getMessage());
        }
        return list;
    }

    @Override
    public Result update(Patient p) {
        String sql = "UPDATE patients SET name=?, age=?, gender=?, medical_history=? WHERE patient_id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, p.getName());
            ps.setInt(2, p.getAge());
            ps.setString(3, p.getGender());
            ps.setString(4, p.getMedicalHistory());
            ps.setInt(5, p.getPatientId());
            return ps.executeUpdate() == 1 ? Result.ok() : Result.fail("Patient not found");
        } catch (SQLException e) {
            return Result.fail("Database error: " + e.getMessage());
        }
    }

    private Patient map(ResultSet rs) throws SQLException {
        return new Patient(
            rs.getInt("patient_id"),
            rs.getString("name"),
            rs.getInt("age"),
            rs.getString("gender"),
            rs.getString("medical_history")
        );
    }
}
