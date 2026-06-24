package dao;

import db.DatabaseConnection;
import model.Doctor;
import model.Result;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DoctorDAOImpl implements DoctorDAO {
    private final Connection conn = DatabaseConnection.getInstance().get();

    @Override
    public Result insert(Doctor d) {
        String sql = "INSERT INTO doctors(name, specialization) VALUES (?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, d.getName());
            ps.setString(2, d.getSpecialization());
            ps.executeUpdate();
            return Result.ok();
        } catch (SQLException e) {
            return Result.fail("Database error: " + e.getMessage());
        }
    }

    @Override
    public Optional<Doctor> findById(int id) {
        try (PreparedStatement ps = conn.prepareStatement("SELECT * FROM doctors WHERE doctor_id=?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(map(rs));
            }
        } catch (SQLException e) {
            System.err.println("[DoctorDAO.findById] " + e.getMessage());
        }
        return Optional.empty();
    }

    @Override
    public List<Doctor> findAll() {
        List<Doctor> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement("SELECT * FROM doctors ORDER BY doctor_id");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) {
            System.err.println("[DoctorDAO.findAll] " + e.getMessage());
        }
        return list;
    }

    @Override
    public Result update(Doctor d) {
        String sql = "UPDATE doctors SET name=?, specialization=? WHERE doctor_id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, d.getName());
            ps.setString(2, d.getSpecialization());
            ps.setInt(3, d.getDoctorId());
            return ps.executeUpdate() == 1 ? Result.ok() : Result.fail("Doctor not found");
        } catch (SQLException e) {
            return Result.fail("Database error: " + e.getMessage());
        }
    }

    private Doctor map(ResultSet rs) throws SQLException {
        return new Doctor(
            rs.getInt("doctor_id"),
            rs.getString("name"),
            rs.getString("specialization")
        );
    }
}
