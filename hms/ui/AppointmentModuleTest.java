package hms.ui;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

/**
 * Standalone launcher for the Appointment module, used for development and the
 * demo video (Section 4). In the final integrated system, Member D's Main.java
 * will create the AppointmentPanel instead of this class.
 */
public class AppointmentModuleTest {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("HMS \u2014 Appointment Booking (Module 2)");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setContentPane(new AppointmentPanel());
            frame.setSize(820, 520);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}
