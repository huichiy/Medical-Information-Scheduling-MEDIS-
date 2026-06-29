package view;

import controller.SystemController;
import dao.AppointmentDAOImpl;
import model.Appointment;
import model.Doctor;
import model.Patient;
import model.Result;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

public class AppointmentPanel extends JPanel {
    private static final DateTimeFormatter FMT = AppointmentDAOImpl.FMT; // "yyyy-MM-dd HH:mm"

    private final SystemController system;
    private final DefaultTableModel model = new DefaultTableModel(
        new String[]{"ID", "Patient", "Doctor", "Date & Time", "Status"}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable table = new JTable(model);

    private final JComboBox<Patient> patientBox = new JComboBox<>();
    private final JComboBox<Doctor>  doctorBox  = new JComboBox<>();
    private final JTextField         dtField    = new JTextField("2026-07-01 14:00", 14);

    public AppointmentPanel(SystemController system) {
        this.system = system;
        setLayout(new BorderLayout());
        add(buildForm(),   BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
        add(buildBottom(), BorderLayout.SOUTH);
        reloadDropdowns();
        refresh();
    }

    private JComponent buildForm() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT));
        p.add(new JLabel("Patient:")); p.add(patientBox);
        p.add(new JLabel("Doctor:"));  p.add(doctorBox);
        p.add(new JLabel("When (yyyy-MM-dd HH:mm):")); p.add(dtField);
        JButton book = new JButton("Book");
        book.addActionListener(e -> doBook());
        p.add(book);
        return p;
    }

    private JComponent buildBottom() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton cancel = new JButton("Cancel Selected");
        cancel.addActionListener(e -> doCancel());
        p.add(cancel);
        JButton refresh = new JButton("Refresh");
        refresh.addActionListener(e -> { reloadDropdowns(); refresh(); });
        p.add(refresh);
        return p;
    }

    private void doBook() {
        Patient p = (Patient) patientBox.getSelectedItem();
        Doctor  d = (Doctor)  doctorBox.getSelectedItem();
        if (p == null) { DialogHelper.showError(this, "Please select a patient"); return; }
        if (d == null) { DialogHelper.showError(this, "Please select a doctor");  return; }

        LocalDateTime dt;
        try {
            dt = LocalDateTime.parse(dtField.getText().trim(), FMT);
        } catch (DateTimeParseException ex) {
            DialogHelper.showError(this, "Invalid date/time. Use format: yyyy-MM-dd HH:mm");
            return;
        }

        Result r = system.appointment().book(p.getPatientId(), d.getDoctorId(), dt);
        if (r.isOk()) {
            DialogHelper.showInfo(this, "Appointment booked");
            refresh();
        } else {
            DialogHelper.showError(this, r.getMessage());
        }
    }

    private void doCancel() {
        int row = table.getSelectedRow();
        if (row < 0) { DialogHelper.showError(this, "Select an appointment to cancel"); return; }
        int id = (int) model.getValueAt(row, 0);
        if (!DialogHelper.confirm(this, "Cancel appointment #" + id + "?")) return;
        Result r = system.appointment().cancel(id);
        if (r.isOk()) { DialogHelper.showInfo(this, "Appointment cancelled"); refresh(); }
        else          { DialogHelper.showError(this, r.getMessage()); }
    }

    private void reloadDropdowns() {
        patientBox.removeAllItems();
        for (Patient p : system.patient().getAll()) patientBox.addItem(p);
        doctorBox.removeAllItems();
        for (Doctor d : system.doctor().getAll()) doctorBox.addItem(d);
    }

    private void refresh() {
        model.setRowCount(0);
        List<Appointment> appts = system.appointment().getAll();
        for (Appointment a : appts) {
            if (a.getStatus() == Appointment.Status.CANCELLED) continue; // hide cancelled
            model.addRow(new Object[]{
                a.getAppointmentId(),
                a.getPatient() == null ? "?" : a.getPatient().getName(),
                a.getDoctor()  == null ? "?" : a.getDoctor().getName(),
                a.getDateTime().format(FMT),
                a.getStatus()
            });
        }
    }
}
