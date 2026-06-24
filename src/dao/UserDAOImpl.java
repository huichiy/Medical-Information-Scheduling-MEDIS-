package dao;

import db.DatabaseConnection;
import model.Admin;
import model.Doctor;
import model.Receptionist;
import model.User;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserDAOImpl implements UserDAO {
    private final Connection conn = DatabaseConnection.getInstance().get();

    private static final String SELECT =
        "SELECT u.user_id, u.username, u.password, u.role, " +
        "       d.doctor_id, d.name, d.specialization " +
        "FROM users u LEFT JOIN doctors d ON d.user_id = u.user_id ";

    @Override
    public Optional<User> findByUsername(String username) {
        try (PreparedStatement ps = conn.prepareStatement(SELECT + "WHERE u.username = ?")) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapUser(rs));
            }
        } catch (SQLException e) {
            System.err.println("[UserDAO.findByUsername] " + e.getMessage());
        }
        return Optional.empty();
    }

    @Override
    public List<User> findAll() {
        List<User> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(SELECT);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapUser(rs));
        } catch (SQLException e) {
            System.err.println("[UserDAO.findAll] " + e.getMessage());
        }
        return list;
    }

    private User mapUser(ResultSet rs) throws SQLException {
        int    id   = rs.getInt("user_id");
        String un   = rs.getString("username");
        String hash = rs.getString("password");
        String role = rs.getString("role");
        switch (role) {
            case "ADMIN":        return new Admin(id, un, hash);
            case "RECEPTIONIST": return new Receptionist(id, un, hash);
            case "DOCTOR":
                return new Doctor(id, un, hash,
                                  rs.getInt("doctor_id"),
                                  rs.getString("name"),
                                  rs.getString("specialization"));
            default: throw new SQLException("Unknown role: " + role);
        }
    }
}
