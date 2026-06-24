package view;

import controller.SystemController;
import dao.AppointmentDAOImpl;
import model.Appointment;
import model.Doctor;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

public class ReportPanel extends JPanel {
    private static final DateTimeFormatter FMT = AppointmentDAOImpl.FMT;

    private final SystemController system;
    private final JLabel totalPatientsLbl     = new JLabel();
    private final JLabel totalAppointmentsLbl = new JLabel();
    private final DefaultTableModel scheduleModel = new DefaultTableModel(
        new String[]{"Doctor", "Specialization", "Date & Time", "Patient", "Status"}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };

    public ReportPanel(SystemController system) {
        this.system = system;
        setLayout(new BorderLayout());
        add(buildHeader(), BorderLayout.NORTH);
        add(new JScrollPane(new JTable(scheduleModel)), BorderLayout.CENTER);
        refresh();
    }

    private JComponent buildHeader() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT));
        totalPatientsLbl.setFont(totalPatientsLbl.getFont().deriveFont(Font.BOLD, 14f));
        totalAppointmentsLbl.setFont(totalAppointmentsLbl.getFont().deriveFont(Font.BOLD, 14f));
        p.add(totalPatientsLbl);
        p.add(Box.createHorizontalStrut(24));
        p.add(totalAppointmentsLbl);
        p.add(Box.createHorizontalStrut(24));
        JButton btn = new JButton("Refresh Report");
        btn.addActionListener(e -> refresh());
        p.add(btn);
        return p;
    }

    private void refresh() {
        totalPatientsLbl.setText("Total Patients: " + system.report().totalPatients());
        totalAppointmentsLbl.setText("Total Appointments: " + system.report().totalAppointments());
        scheduleModel.setRowCount(0);
        Map<Doctor, List<Appointment>> schedules = system.report().doctorSchedules();
        for (Map.Entry<Doctor, List<Appointment>> e : schedules.entrySet()) {
            Doctor d = e.getKey();
            if (e.getValue().isEmpty()) {
                scheduleModel.addRow(new Object[]{ d.getName(), d.getSpecialization(), "-", "-", "-" });
            } else {
                for (Appointment a : e.getValue()) {
                    scheduleModel.addRow(new Object[]{
                        d.getName(), d.getSpecialization(),
                        a.getDateTime().format(FMT),
                        a.getPatient() == null ? "?" : a.getPatient().getName(),
                        a.getStatus()
                    });
                }
            }
        }
    }
}
