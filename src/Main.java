import controller.AppointmentController;
import controller.DoctorController;
import controller.LoginController;
import controller.PatientController;
import controller.ReportController;
import controller.SystemController;
import dao.AppointmentDAO;
import dao.AppointmentDAOImpl;
import dao.DoctorDAO;
import dao.DoctorDAOImpl;
import dao.PatientDAO;
import dao.PatientDAOImpl;
import dao.UserDAO;
import dao.UserDAOImpl;
import view.LoginFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class Main {
    public static void main(String[] args) {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            System.err.println("FATAL: sqlite-jdbc not on classpath.");
            System.err.println("Run with:  java -cp \"lib/*:bin\" Main");
            System.exit(1);
        }

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
            // fall back to default look and feel
        }

        // DAOs
        UserDAO        userDAO        = new UserDAOImpl();
        PatientDAO     patientDAO     = new PatientDAOImpl();
        DoctorDAO      doctorDAO      = new DoctorDAOImpl();
        AppointmentDAO appointmentDAO = new AppointmentDAOImpl();

        // Controllers (inject DAOs)
        LoginController       loginCtrl  = new LoginController(userDAO);
        PatientController     patientCtrl = new PatientController(patientDAO);
        DoctorController      doctorCtrl  = new DoctorController(doctorDAO);
        AppointmentController apptCtrl    = new AppointmentController(appointmentDAO, patientDAO, doctorDAO);
        ReportController      reportCtrl  = new ReportController(patientDAO, doctorDAO, appointmentDAO);

        SystemController system =
            new SystemController(loginCtrl, patientCtrl, doctorCtrl, apptCtrl, reportCtrl);

        SwingUtilities.invokeLater(() -> new LoginFrame(system).setVisible(true));
    }
}
