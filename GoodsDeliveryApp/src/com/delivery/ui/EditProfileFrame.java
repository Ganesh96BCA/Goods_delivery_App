package com.delivery.ui;

import javax.swing.*;
import com.delivery.util.DBConnection;
import java.awt.*;
import java.sql.*;

public class EditProfileFrame extends JFrame {

    private JTextField firstNameField;
    private JTextField lastNameField;
    private JTextField emailField;
    private JPasswordField passwordField;
    private JTextField phoneField;
    private JTextField truckRegField;
    private JTextField truckCapacityField;

    private JPanel truckPanel;

    private String email;
    private String role;

    public EditProfileFrame(String email, String role) {

        this.email = email;
        this.role = role;

        setTitle("Edit Profile Information");
        setSize(500, 620);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        mainPanel.setBackground(Color.WHITE);

        JLabel title = new JLabel("Edit Profile Information");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        mainPanel.add(title);
        mainPanel.add(Box.createVerticalStrut(20));

        firstNameField = createText(mainPanel, "First Name");
        lastNameField = createText(mainPanel, "Last Name");

        emailField = createText(mainPanel, "Email Address");
        emailField.setEditable(false);

        passwordField = createPassword(mainPanel, "Password");
        phoneField = createText(mainPanel, "Phone Number");

        truckPanel = new JPanel();
        truckPanel.setLayout(new BoxLayout(truckPanel, BoxLayout.Y_AXIS));
        truckPanel.setBackground(Color.WHITE);

        truckRegField = createText(truckPanel, "Truck Registration Number");
        truckCapacityField = createText(truckPanel, "Truck Capacity (KGS)");

        if ("Driver".equalsIgnoreCase(role)) {
            mainPanel.add(truckPanel);
        }

        mainPanel.add(Box.createVerticalStrut(20));

        JButton saveBtn = new JButton("Save Changes");
        JButton cancelBtn = new JButton("No Thanks");

        saveBtn.addActionListener(e -> {
            if (updateProfile()) {
                openDashboard();
            }
        });

        cancelBtn.addActionListener(e -> openDashboard());

        JPanel btnPanel = new JPanel();
        btnPanel.add(saveBtn);
        btnPanel.add(cancelBtn);

        mainPanel.add(btnPanel);
        add(mainPanel);

        loadUserData();
    }

    // -------- LOAD USER DATA --------
    private void loadUserData() {
        try (Connection con = DBConnection.getConnection()) {

            String sql = "SELECT * FROM users WHERE email=?";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, email);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                firstNameField.setText(rs.getString("first_name"));
                lastNameField.setText(rs.getString("last_name"));
                emailField.setText(rs.getString("email"));
                passwordField.setText(rs.getString("password"));
                phoneField.setText(rs.getString("phone"));

                if ("Driver".equalsIgnoreCase(role)) {
                    truckRegField.setText(rs.getString("truck_number"));
                    truckCapacityField.setText(rs.getString("truck_capacity"));
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // -------- UPDATE PROFILE (ALL VALIDATIONS) --------
    private boolean updateProfile() {

        String firstName = firstNameField.getText();
        String lastName = lastNameField.getText();
        String password = new String(passwordField.getPassword());
        String phone = phoneField.getText();

        if (firstName.replaceAll("\\s+", "").isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter First Name");
            firstNameField.requestFocus();
            return false;
        }

        if (lastName.replaceAll("\\s+", "").isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter Last Name");
            lastNameField.requestFocus();
            return false;
        }

        if (password.replaceAll("\\s+", "").isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter Password");
            passwordField.requestFocus();
            return false;
        }

        // ✅ FIXED: PHONE NUMBER VALIDATION
        if (phone.replaceAll("\\s+", "").isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter Phone Number");
            phoneField.requestFocus();
            return false;
        }

        if ("Driver".equalsIgnoreCase(role)) {

            if (truckRegField.getText().replaceAll("\\s+", "").isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter Truck Registration Number");
                truckRegField.requestFocus();
                return false;
            }

            if (truckCapacityField.getText().replaceAll("\\s+", "").isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter Truck Capacity");
                truckCapacityField.requestFocus();
                return false;
            }
        }

        try (Connection con = DBConnection.getConnection()) {

            String sql = "UPDATE users SET first_name=?, last_name=?, password=?, phone=?, truck_number=?, truck_capacity=? WHERE email=?";
            PreparedStatement ps = con.prepareStatement(sql);

            ps.setString(1, firstName);
            ps.setString(2, lastName);
            ps.setString(3, password);
            ps.setString(4, phone);

            if ("Driver".equalsIgnoreCase(role)) {
                ps.setString(5, truckRegField.getText());
                ps.setString(6, truckCapacityField.getText());
            } else {
                ps.setNull(5, Types.VARCHAR);
                ps.setNull(6, Types.VARCHAR);
            }

            ps.setString(7, email);
            ps.executeUpdate();

            JOptionPane.showMessageDialog(this, "Profile Updated Successfully");
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private void openDashboard() {

        if ("Customer".equalsIgnoreCase(role)) {
            new CustomerDashboard(email).setVisible(true);
        } else if ("Scheduler".equalsIgnoreCase(role)) {
            new SchedulerDashboard(email).setVisible(true);
        } else if ("Driver".equalsIgnoreCase(role)) {
            new DriverDashboard(email).setVisible(true);
        }

        dispose();
    }

    private JTextField createText(JPanel panel, String label) {
        JLabel l = new JLabel(label);
        JTextField t = new JTextField();
        t.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));

        panel.add(l);
        panel.add(Box.createVerticalStrut(5));
        panel.add(t);
        panel.add(Box.createVerticalStrut(15));

        return t;
    }

    private JPasswordField createPassword(JPanel panel, String label) {
        JLabel l = new JLabel(label);
        JPasswordField p = new JPasswordField();
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));

        panel.add(l);
        panel.add(Box.createVerticalStrut(5));
        panel.add(p);
        panel.add(Box.createVerticalStrut(15));

        return p;
    }
}
