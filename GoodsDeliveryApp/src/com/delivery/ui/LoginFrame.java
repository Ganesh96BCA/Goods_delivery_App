package com.delivery.ui;

import javax.swing.*;

import com.delivery.util.DBConnection;

import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class LoginFrame extends JFrame {

    private JTextField emailField;
    private JPasswordField passwordField;

    public LoginFrame() {

        setTitle("Goods Delivery System - Login");
        setSize(450, 420);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel mainPanel = new JPanel();
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Goods Delivery System");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitle = new JLabel("Please log in to your account");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        mainPanel.add(title);
        mainPanel.add(Box.createVerticalStrut(10));
        mainPanel.add(subtitle);
        mainPanel.add(Box.createVerticalStrut(25));

        JLabel emailLabel = new JLabel("Email Address");
        emailField = new JTextField();
        emailField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));

        mainPanel.add(emailLabel);
        mainPanel.add(Box.createVerticalStrut(5));
        mainPanel.add(emailField);
        mainPanel.add(Box.createVerticalStrut(15));

        JLabel passLabel = new JLabel("Password");
        passwordField = new JPasswordField();
        passwordField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));

        mainPanel.add(passLabel);
        mainPanel.add(Box.createVerticalStrut(5));
        mainPanel.add(passwordField);
        mainPanel.add(Box.createVerticalStrut(25));

        JButton authenticateBtn = new JButton("Authenticate");
        JButton registerBtn = new JButton("Register");

        authenticateBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        registerBtn.setAlignmentX(Component.CENTER_ALIGNMENT);

        authenticateBtn.setPreferredSize(new Dimension(200, 40));
        registerBtn.setPreferredSize(new Dimension(200, 40));

        authenticateBtn.setBackground(new Color(30, 144, 255));
        authenticateBtn.setForeground(Color.WHITE);

        registerBtn.setBackground(new Color(46, 204, 113));
        registerBtn.setForeground(Color.WHITE);

        // Authenticate → Edit Profile
        authenticateBtn.addActionListener(e -> loginUser());

        // ✅ Register → RegisterFrame
        registerBtn.addActionListener(e -> {
            new RegisterFrame().setVisible(true);
            dispose();
        });

        mainPanel.add(authenticateBtn);
        mainPanel.add(Box.createVerticalStrut(15));
        mainPanel.add(registerBtn);

        add(mainPanel);
    }

    /*public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new LoginFrame().setVisible(true);
        });
    }*/
    private void loginUser() {

        String email = emailField.getText();
        String password = new String(passwordField.getPassword());

        if (email.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Enter email & password");
            return;
        }

        try (Connection con = DBConnection.getConnection()) {

            String sql = "SELECT role FROM users WHERE email=? AND password=?";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, email);
            ps.setString(2, password);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                String role = rs.getString("role");

                new EditProfileFrame(email, role).setVisible(true);
                dispose();

            } else {
                JOptionPane.showMessageDialog(this, "Invalid Login");
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}
