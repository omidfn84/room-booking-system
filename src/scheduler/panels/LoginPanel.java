package scheduler.panels;

import javax.swing.*;

import scheduler.accounts.RegisteredUser;
import scheduler.gui.GUIController;

import java.awt.*;

public class LoginPanel extends JPanel {
    private final GUIController controller;
    private final Runnable onLoginSuccess;

    private JTextField emailField = new JTextField(18);
    private JPasswordField passwordField = new JPasswordField(18);
    private JTextField userNameField = new JTextField(18);
    private JComboBox<String> accountTypeBox = new JComboBox<>(new String[]{"STUDENT", "FACULTY", "STAFF", "PARTNER"});
    private JTextField idField = new JTextField(18);
    private JLabel statusLabel = new JLabel(" ");

    public LoginPanel(GUIController controller, Runnable onLoginSuccess) {
        this.controller = controller;
        this.onLoginSuccess = onLoginSuccess;
        setBorder(new javax.swing.border.EmptyBorder(12, 12, 12, 12));
        buildUI();
    }

    private void buildUI() {
        setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(5, 5, 5, 5);
        c.anchor = GridBagConstraints.WEST;

        int row = 0;
        addRow(c, row++, "Email:", emailField);
        addRow(c, row++, "Password:", passwordField);
        addRow(c, row++, "Username (for registration):", userNameField);
        addRow(c, row++, "Account type (for registration):", accountTypeBox);
        addRow(c, row++, "Student ID / Org ID (for registration):", idField);

        JButton loginBtn = new JButton("Login");
        JButton registerBtn = new JButton("Register");

        JPanel buttons = new JPanel();
        buttons.add(loginBtn);
        buttons.add(registerBtn);

        c.gridx = 0; c.gridy = row; c.gridwidth = 2;
        add(buttons, c);
        row++;
        c.gridy = row; c.gridwidth = 2;
        add(statusLabel, c);

        loginBtn.addActionListener(e -> handleLogin());
        registerBtn.addActionListener(e -> handleRegister());
    }

    private void addRow(GridBagConstraints c, int row, String label, JComponent field) {
        c.gridx = 0; c.gridy = row; c.gridwidth = 1;
        add(new JLabel(label), c);
        c.gridx = 1;
        add(field, c);
    }

    private void handleLogin() {
        try {
            RegisteredUser user = controller.onLoginClicked(emailField.getText().trim(), new String(passwordField.getPassword()));
            if (user == null) {
                statusLabel.setText("Login failed: invalid email or password.");
                statusLabel.setForeground(Color.RED);
            } else {
                statusLabel.setText("Logged in as " + user.getUserName());
                statusLabel.setForeground(new Color(0, 128, 0));
                onLoginSuccess.run();
            }
        } catch (Exception ex) {
            statusLabel.setText("Error: " + ex.getMessage());
            statusLabel.setForeground(Color.RED);
        }
    }

    private void handleRegister() {
        try {
            // pass the raw text: AccountManagement enforces the 9-digit rule (Req8)
            RegisteredUser user = controller.onRegisterClicked(
                    emailField.getText().trim(),
                    new String(passwordField.getPassword()),
                    userNameField.getText().trim(),
                    (String) accountTypeBox.getSelectedItem(),
                    idField.getText().trim()
            );
            statusLabel.setText("Account created for " + user.getUserName() + ". You are now logged in.");
            statusLabel.setForeground(new Color(0, 128, 0));
            onLoginSuccess.run();
        } catch (Exception ex) {
            statusLabel.setText("Error: " + ex.getMessage());
            statusLabel.setForeground(Color.RED);
        }
    }
}