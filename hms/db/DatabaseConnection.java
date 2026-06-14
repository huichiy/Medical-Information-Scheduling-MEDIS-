package hms.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * JDBC connection helper.
 *
 * NOTE FOR THE GROUP: This file belongs to Member A (DB / Infra — JDBC).
 * It is included here as a compatible version so the Appointment module can be
 * compiled and tested on its own. When integrating, use Member A's class and
 * delete this one if it conflicts.
 *
 * Requires the MySQL Connector/J .jar on the classpath at runtime.
 * Defaults below match a standard XAMPP MySQL install (user "root", no password).
 */
public class DatabaseConnection {

    private static final String URL = "jdbc:mysql://localhost:3307/hms";
    private static final String USER = "root";
    private static final String PASSWORD = ""; // XAMPP default: empty password

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
