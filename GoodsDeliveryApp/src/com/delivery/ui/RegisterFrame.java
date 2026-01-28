package com.delivery.ui;

import javax.swing.*;
import com.delivery.util.DBConnection;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class RegisterFrame extends JFrame {

    private JTextField firstNameField;
    private JTextField lastNameField;
    private JTextField emailField;
    private JPasswordField passwordField;
    private JTextField phoneField;
    private JComboBox<String> roleCombo;
    private JTextField truckRegField;
    private JTextField truckCapacityField;

    private JPanel truckPanel;

    public RegisterFrame() {

        setTitle("Register");
        setSize(480, 720);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        /* ================= TOP PANEL (BACK BUTTON) ================= */
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        topPanel.setBackground(new Color(245, 247, 249));

        JButton backBtn = new JButton("← Back");
        backBtn.setFocusPainted(false);
        backBtn.addActionListener(e -> {
            new LoginFrame().setVisible(true);
            dispose();
        });

        topPanel.add(backBtn, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        /* ================= MAIN PANEL ================= */
        JPanel mainPanel = new JPanel();
        mainPanel.setBackground(new Color(245, 247, 249));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(25, 35, 25, 35));
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Register");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        mainPanel.add(title);
        mainPanel.add(Box.createVerticalStrut(25));

        firstNameField = createTextField(mainPanel, "First Name");
        lastNameField = createTextField(mainPanel, "Last Name");
        emailField = createTextField(mainPanel, "Email Address");
        passwordField = createPasswordField(mainPanel, "Password");
        phoneField = createTextField(mainPanel, "Cell Phone Number");

        JLabel roleLabel = new JLabel("Role");
        roleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        roleCombo = new JComboBox<>(new String[]{"Customer", "Scheduler", "Driver"});
        roleCombo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));

        mainPanel.add(roleLabel);
        mainPanel.add(Box.createVerticalStrut(5));
        mainPanel.add(roleCombo);
        mainPanel.add(Box.createVerticalStrut(15));

        truckPanel = new JPanel();
        truckPanel.setLayout(new BoxLayout(truckPanel, BoxLayout.Y_AXIS));
        truckPanel.setBackground(mainPanel.getBackground());

        truckRegField = createTextField(truckPanel, "Truck Registration No");
        truckCapacityField = createTextField(truckPanel, "Truck Capacity (KGS)");

        truckPanel.setVisible(false);
        mainPanel.add(truckPanel);

        mainPanel.add(Box.createVerticalStrut(25));

        JButton registerBtn = new JButton("Register");
        registerBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        registerBtn.setPreferredSize(new Dimension(150, 40));
        registerBtn.setMaximumSize(new Dimension(150, 40));
        registerBtn.setBackground(new Color(90, 120, 190));
        registerBtn.setForeground(Color.WHITE);

        registerBtn.addActionListener(e -> registerUser());
        mainPanel.add(registerBtn);

        add(mainPanel, BorderLayout.CENTER);

        roleCombo.addActionListener(e -> handleRoleChange());
    }

    private void handleRoleChange() {
        String role = (String) roleCombo.getSelectedItem();
        truckPanel.setVisible("Driver".equals(role));
        revalidate();
        repaint();
    }

    private JTextField createTextField(JPanel panel, String labelText) {
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        JTextField field = new JTextField();
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));

        panel.add(label);
        panel.add(Box.createVerticalStrut(5));
        panel.add(field);
        panel.add(Box.createVerticalStrut(15));

        return field;
    }

    private JPasswordField createPasswordField(JPanel panel, String labelText) {
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        JPasswordField field = new JPasswordField();
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));

        panel.add(label);
        panel.add(Box.createVerticalStrut(5));
        panel.add(field);
        panel.add(Box.createVerticalStrut(15));

        return field;
    }

    private void registerUser() {

        String firstName = firstNameField.getText();
        String lastName = lastNameField.getText();
        String email = emailField.getText();
        String password = new String(passwordField.getPassword());
        String phone = phoneField.getText();
        String role = (String) roleCombo.getSelectedItem();

        String truckReg = truckPanel.isVisible() ? truckRegField.getText() : null;
        String truckCapacity = truckPanel.isVisible() ? truckCapacityField.getText() : null;

        if (firstName.isEmpty() || lastName.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please enter first name or last name",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (phone.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Phone number is required",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (email.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Email and Password are required",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if ("Driver".equals(role) &&
                (truckReg == null || truckReg.isEmpty()
                        || truckCapacity == null || truckCapacity.isEmpty())) {

            JOptionPane.showMessageDialog(this,
                    "Please enter truck registration and capacity",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try (Connection con = DBConnection.getConnection()) {

            /* 🔍 STEP 1: CHECK DUPLICATE EMAIL */
            String checkSql = "SELECT COUNT(*) FROM users WHERE email = ?";
            PreparedStatement checkPs = con.prepareStatement(checkSql);
            checkPs.setString(1, email);

            ResultSet rs = checkPs.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                JOptionPane.showMessageDialog(this,
                        "Email already taken. Please provide a new email id.",
                        "Duplicate Email", JOptionPane.ERROR_MESSAGE);
                return;
            }

            /* ✅ STEP 2: INSERT USER */
            String insertSql =
                    "INSERT INTO users " +
                    "(first_name, last_name, email, password, phone, role, truck_number, truck_capacity) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

            PreparedStatement ps = con.prepareStatement(insertSql);
            ps.setString(1, firstName);
            ps.setString(2, lastName);
            ps.setString(3, email);
            ps.setString(4, password);
            ps.setString(5, phone);
            ps.setString(6, role);
            ps.setString(7, truckReg);
            ps.setString(8, truckCapacity);

            ps.executeUpdate();

            JOptionPane.showMessageDialog(this,
                    "Registration Successful! Please login.",
                    "Success", JOptionPane.INFORMATION_MESSAGE);

            new LoginFrame().setVisible(true);
            dispose();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Something went wrong. Please try again.",
                    "Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

}
