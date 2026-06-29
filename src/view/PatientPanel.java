package view;

import controller.SystemController;
import model.Patient;
import model.Result;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

public class PatientPanel extends JPanel {
    private final SystemController system;
    private final DefaultTableModel model = new DefaultTableModel(
        new String[]{"ID", "Name", "Age", "Gender", "Medical History"}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable table = new JTable(model);

    private final JTextField        nameField   = new JTextField(14);
    private final JTextField        ageField    = new JTextField(4);
    private final JComboBox<String>  genderBox  = new JComboBox<>(new String[]{"M", "F", "Other"});
    private final JTextArea         historyArea = new JTextArea(3, 20);

    public PatientPanel(SystemController system) {
        this.system = system;
        setLayout(new BorderLayout());
        add(buildForm(), BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);

        // MouseListener: clicking a row loads its values into the form
        // so the user can edit them and press "Update Selected".
        table.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                int row = table.getSelectedRow();
                if (row < 0) return;
                nameField.setText(String.valueOf(model.getValueAt(row, 1)));
                ageField.setText(String.valueOf(model.getValueAt(row, 2)));
                genderBox.setSelectedItem(model.getValueAt(row, 3));
                historyArea.setText(String.valueOf(model.getValueAt(row, 4)));
            }
        });

        refresh();
    }

    private JComponent buildForm() {
        JPanel p = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 4, 4, 4);
        c.anchor = GridBagConstraints.WEST;

        c.gridx = 0; c.gridy = 0; p.add(new JLabel("Name:"), c);
        c.gridx = 1; p.add(nameField, c);
        c.gridx = 2; p.add(new JLabel("Age:"), c);
        c.gridx = 3; p.add(ageField, c);
        c.gridx = 4; p.add(new JLabel("Gender:"), c);
        c.gridx = 5; p.add(genderBox, c);
        c.gridx = 0; c.gridy = 1; p.add(new JLabel("History:"), c);
        c.gridx = 1; c.gridwidth = 5; p.add(new JScrollPane(historyArea), c);
        c.gridwidth = 1;

        JButton addBtn = new JButton("Add Patient");
        addBtn.addActionListener(e -> doAdd());
        c.gridx = 0; c.gridy = 2; p.add(addBtn, c);

        JButton updateBtn = new JButton("Update Selected");
        updateBtn.addActionListener(e -> doUpdate());
        c.gridx = 1; p.add(updateBtn, c);

        JButton clearBtn = new JButton("Clear");
        clearBtn.addActionListener(e -> clearForm());
        c.gridx = 2; p.add(clearBtn, c);

        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.addActionListener(e -> refresh());
        c.gridx = 3; p.add(refreshBtn, c);

        return p;
    }

    private void doAdd() {
        int age;
        try {
            age = Integer.parseInt(ageField.getText().trim());
        } catch (NumberFormatException ex) {
            DialogHelper.showError(this, "Age must be a number");
            return;
        }
        Result r = system.patient().add(
            nameField.getText(), age,
            (String) genderBox.getSelectedItem(), historyArea.getText());
        if (r.isOk()) {
            DialogHelper.showInfo(this, "Patient added");
            clearForm();
            refresh();
        } else {
            DialogHelper.showError(this, r.getMessage());
        }
    }

    private void doUpdate() {
        int row = table.getSelectedRow();
        if (row < 0) {
            DialogHelper.showError(this, "Select a patient to update");
            return;
        }
        int id = (int) model.getValueAt(row, 0);
        int age;
        try {
            age = Integer.parseInt(ageField.getText().trim());
        } catch (NumberFormatException ex) {
            DialogHelper.showError(this, "Age must be a number");
            return;
        }
        Result r = system.patient().update(
            id, nameField.getText(), age,
            (String) genderBox.getSelectedItem(), historyArea.getText());
        if (r.isOk()) {
            DialogHelper.showInfo(this, "Patient updated");
            refresh();
        } else {
            DialogHelper.showError(this, r.getMessage());
        }
    }

    private void clearForm() {
        nameField.setText("");
        ageField.setText("");
        genderBox.setSelectedIndex(0);
        historyArea.setText("");
        table.clearSelection();
    }

    private void refresh() {
        model.setRowCount(0);
        List<Patient> patients = system.patient().getAll();
        for (Patient pt : patients) {
            model.addRow(new Object[]{
                pt.getPatientId(), pt.getName(), pt.getAge(),
                pt.getGender(), pt.getMedicalHistory()
            });
        }
    }
}