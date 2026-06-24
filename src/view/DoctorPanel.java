package view;

import controller.SystemController;
import model.Doctor;
import model.Result;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class DoctorPanel extends JPanel {
    private final SystemController system;
    private final DefaultTableModel model = new DefaultTableModel(
        new String[]{"ID", "Name", "Specialization"}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable table = new JTable(model);

    private final JTextField nameField = new JTextField(14);
    private final JTextField specField = new JTextField(14);

    public DoctorPanel(SystemController system) {
        this.system = system;
        setLayout(new BorderLayout());
        add(buildForm(), BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
        refresh();
    }

    private JComponent buildForm() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT));
        p.add(new JLabel("Name:"));           p.add(nameField);
        p.add(new JLabel("Specialization:")); p.add(specField);
        JButton addBtn = new JButton("Add Doctor");
        addBtn.addActionListener(e -> doAdd());
        p.add(addBtn);
        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.addActionListener(e -> refresh());
        p.add(refreshBtn);
        return p;
    }

    private void doAdd() {
        Result r = system.doctor().add(nameField.getText(), specField.getText());
        if (r.isOk()) {
            DialogHelper.showInfo(this, "Doctor added");
            nameField.setText("");
            specField.setText("");
            refresh();
        } else {
            DialogHelper.showError(this, r.getMessage());
        }
    }

    private void refresh() {
        model.setRowCount(0);
        List<Doctor> doctors = system.doctor().getAll();
        for (Doctor d : doctors) {
            model.addRow(new Object[]{ d.getDoctorId(), d.getName(), d.getSpecialization() });
        }
    }
}
