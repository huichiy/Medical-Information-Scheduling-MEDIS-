package hms.ui;

import hms.dao.AppointmentDAO;
import hms.exception.DuplicateSlotException;
import hms.model.Appointment;
import hms.model.Doctor;
import hms.model.Patient;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

/**
 * Swing UI for Module 2 — Appointment Booking.
 *
 * Built as a JPanel so Member D's Main.java / SystemController can drop it into
 * the main application (e.g. as a tab). Run AppointmentModuleTest to launch it
 * standalone for development and the demo.
 *
 * Event handling (required by the assignment):
 *   - ActionListener : the "Book Appointment" button.
 *   - MouseListener  : clicking a row in the appointment table shows details.
 */
public class AppointmentPanel extends JPanel {

    private final AppointmentDAO dao = new AppointmentDAO();

    private JComboBox<Patient> patientCombo;
    private JComboBox<Doctor> doctorCombo;
    private JTextField dateField;
    private JComboBox<String> timeCombo;
    private JButton bookButton;
    private JTable appointmentTable;
    private DefaultTableModel tableModel;
    private JLabel statusLabel;

    private static final String[] TIME_SLOTS =
            {"09:00", "10:00", "11:00", "14:00", "15:00", "16:00"};

    public AppointmentPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        add(buildFormPanel(), BorderLayout.NORTH);
        add(buildTablePanel(), BorderLayout.CENTER);

        statusLabel = new JLabel(" ");
        add(statusLabel, BorderLayout.SOUTH);

        loadDropdowns();
        refreshTable();
    }

    private JPanel buildFormPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Book Appointment"));

        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(5, 5, 5, 5);
        g.anchor = GridBagConstraints.WEST;

        patientCombo = new JComboBox<>();
        doctorCombo = new JComboBox<>();
        dateField = new JTextField(10);
        dateField.setToolTipText("Format: yyyy-MM-dd, e.g. 2026-07-10");
        timeCombo = new JComboBox<>(TIME_SLOTS);
        bookButton = new JButton("Book Appointment");

        g.gridx = 0; g.gridy = 0; panel.add(new JLabel("Patient:"), g);
        g.gridx = 1; panel.add(patientCombo, g);
        g.gridx = 2; panel.add(new JLabel("Doctor:"), g);
        g.gridx = 3; panel.add(doctorCombo, g);

        g.gridx = 0; g.gridy = 1; panel.add(new JLabel("Date (yyyy-MM-dd):"), g);
        g.gridx = 1; panel.add(dateField, g);
        g.gridx = 2; panel.add(new JLabel("Time:"), g);
        g.gridx = 3; panel.add(timeCombo, g);

        g.gridx = 3; g.gridy = 2; panel.add(bookButton, g);

        // --- ActionListener (required) ---
        bookButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleBooking();
            }
        });

        return panel;
    }

    private JScrollPane buildTablePanel() {
        tableModel = new DefaultTableModel(
                new String[]{"ID", "Patient", "Doctor", "Date", "Time", "Status"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // table is read-only
            }
        };
        appointmentTable = new JTable(tableModel);

        // --- MouseListener (required): click a row to view full details ---
        appointmentTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = appointmentTable.getSelectedRow();
                if (row >= 0) {
                    String info =
                            "Appointment #" + tableModel.getValueAt(row, 0)
                          + "\nPatient : " + tableModel.getValueAt(row, 1)
                          + "\nDoctor  : " + tableModel.getValueAt(row, 2)
                          + "\nDate    : " + tableModel.getValueAt(row, 3)
                          + "\nTime    : " + tableModel.getValueAt(row, 4)
                          + "\nStatus  : " + tableModel.getValueAt(row, 5);
                    JOptionPane.showMessageDialog(AppointmentPanel.this, info,
                            "Appointment Details", JOptionPane.INFORMATION_MESSAGE);
                }
            }
        });

        return new JScrollPane(appointmentTable);
    }

    private void loadDropdowns() {
        try {
            patientCombo.removeAllItems();
            doctorCombo.removeAllItems();
            for (Patient p : dao.getAllPatients()) {
                patientCombo.addItem(p);
            }
            for (Doctor d : dao.getAllDoctors()) {
                doctorCombo.addItem(d);
            }
        } catch (Exception ex) {
            showError("Failed to load patients/doctors: " + ex.getMessage());
        }
    }

    private void refreshTable() {
        tableModel.setRowCount(0);
        try {
            for (Appointment a : dao.getAllAppointments()) {
                tableModel.addRow(new Object[]{
                        a.getAppointmentId(),
                        a.getPatientName(),
                        a.getDoctorName(),
                        a.getDate(),
                        a.getTime(),
                        a.getStatus()
                });
            }
        } catch (Exception ex) {
            showError("Failed to load appointments: " + ex.getMessage());
        }
    }

    private void handleBooking() {
        Patient patient = (Patient) patientCombo.getSelectedItem();
        Doctor doctor = (Doctor) doctorCombo.getSelectedItem();
        String date = dateField.getText().trim();
        String time = (String) timeCombo.getSelectedItem();

        // --- Input validation ---
        if (patient == null || doctor == null) {
            showError("Please select both a patient and a doctor.");
            return;
        }
        if (date.isEmpty()) {
            showError("Please enter a date.");
            return;
        }
        try {
            LocalDate.parse(date); // validates the yyyy-MM-dd format
        } catch (DateTimeParseException ex) {
            showError("Invalid date. Use the format yyyy-MM-dd (e.g. 2026-07-10).");
            return;
        }

        // store time as HH:mm:ss to match the SQL TIME column
        Appointment appt = new Appointment(
                patient.getId(), doctor.getId(), date, time + ":00", "Scheduled");

        try {
            dao.bookAppointment(appt);
            statusLabel.setForeground(new Color(0, 128, 0));
            statusLabel.setText("Appointment booked successfully.");
            dateField.setText("");
            refreshTable();
        } catch (DuplicateSlotException ex) {
            // duplicate time slot prevented
            showError(ex.getMessage());
        } catch (Exception ex) {
            showError("Booking failed: " + ex.getMessage());
        }
    }

    private void showError(String msg) {
        statusLabel.setForeground(Color.RED);
        statusLabel.setText(msg);
        JOptionPane.showMessageDialog(this, msg, "Notice", JOptionPane.WARNING_MESSAGE);
    }
}
