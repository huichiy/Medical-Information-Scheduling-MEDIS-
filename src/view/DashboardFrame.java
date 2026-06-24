package view;

import controller.SystemController;
import model.User;
import javax.swing.*;
import java.awt.*;

public class DashboardFrame extends JFrame {
    private final SystemController system;

    public DashboardFrame(SystemController system) {
        super("MEDIS - " + system.getCurrentUser().dashboardTitle());
        this.system = system;
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 600);
        setLocationRelativeTo(null);
        buildUI();
    }

    private void buildUI() {
        JTabbedPane tabs = new JTabbedPane();
        User u = system.getCurrentUser();
        String role = u.getRole();

        if ("ADMIN".equals(role)) {
            tabs.add("Patients",     new PatientPanel(system));
            tabs.add("Doctors",      new DoctorPanel(system));
            tabs.add("Appointments", new AppointmentPanel(system));
            tabs.add("Reports",      new ReportPanel(system));
        } else if ("DOCTOR".equals(role)) {
            tabs.add("Appointments", new AppointmentPanel(system));
            tabs.add("Patients",     new PatientPanel(system));
        } else if ("RECEPTIONIST".equals(role)) {
            tabs.add("Patients",     new PatientPanel(system));
            tabs.add("Appointments", new AppointmentPanel(system));
        }

        JButton logout = new JButton("Logout");
        logout.addActionListener(e -> {
            system.logout();
            new LoginFrame(system).setVisible(true);
            dispose();
        });

        JPanel top = new JPanel(new BorderLayout());
        top.add(new JLabel("  Logged in as " + u.getUsername() + " (" + role + ")"), BorderLayout.WEST);
        top.add(logout, BorderLayout.EAST);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(top, BorderLayout.NORTH);
        getContentPane().add(tabs, BorderLayout.CENTER);
    }
}
