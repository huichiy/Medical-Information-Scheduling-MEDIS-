package view;

import controller.SystemController;
import model.Result;
import model.User;
import javax.swing.*;
import java.awt.*;

public class LoginFrame extends JFrame {
    private final SystemController system;
    private final JTextField     usernameField = new JTextField(15);
    private final JPasswordField passwordField = new JPasswordField(15);

    public LoginFrame(SystemController system) {
        super("MEDIS - Login");
        this.system = system;
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(360, 200);
        setLocationRelativeTo(null);
        buildUI();
    }

    private void buildUI() {
        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6, 6, 6, 6);

        c.gridx = 0; c.gridy = 0; form.add(new JLabel("Username:"), c);
        c.gridx = 1; form.add(usernameField, c);
        c.gridx = 0; c.gridy = 1; form.add(new JLabel("Password:"), c);
        c.gridx = 1; form.add(passwordField, c);

        JButton loginBtn = new JButton("Login");
        loginBtn.addActionListener(e -> doLogin());
        getRootPane().setDefaultButton(loginBtn);

        c.gridx = 0; c.gridy = 2; c.gridwidth = 2; c.anchor = GridBagConstraints.CENTER;
        form.add(loginBtn, c);

        getContentPane().add(form);
    }

    private void doLogin() {
        String u = usernameField.getText();
        String p = new String(passwordField.getPassword());
        Result r = system.login().authenticate(u, p);
        if (r.isOk()) {
            system.setCurrentUser((User) r.getData());
            new DashboardFrame(system).setVisible(true);
            dispose();
        } else {
            DialogHelper.showError(this, r.getMessage());
        }
    }
}
