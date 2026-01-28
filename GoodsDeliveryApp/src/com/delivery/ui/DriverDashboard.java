package com.delivery.ui;

import com.delivery.util.DBConnection;

import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class DriverDashboard extends JFrame {

    private String driverEmail;

    private JPanel assignedPanel;
    private JPanel completedPanel;

    public DriverDashboard(String email) {
        this.driverEmail = email;

        setTitle("Driver Dashboard");
        setSize(1100, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel mainPanel = new JPanel(new BorderLayout(20, 20));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));
        mainPanel.setBackground(new Color(245, 246, 248));

        /* ================= TOP BAR ================= */
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(new Color(245, 246, 248));

        JLabel title = new JLabel("Driver Dashboard");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));

        JButton logoutBtn = new JButton("Logout");
        logoutBtn.addActionListener(e -> {
            new LoginFrame().setVisible(true);
            dispose();
        });

        topPanel.add(title, BorderLayout.WEST);
        topPanel.add(logoutBtn, BorderLayout.EAST);

        mainPanel.add(topPanel, BorderLayout.NORTH);

        /* ================= CENTER ================= */
        JPanel centerPanel = new JPanel(new GridLayout(1, 2, 20, 0));
        centerPanel.setBackground(new Color(245, 246, 248));

        assignedPanel = createSectionPanel("Assigned Missions");
        completedPanel = createSectionPanel("Completed Missions");

        centerPanel.add(new JScrollPane(assignedPanel));
        centerPanel.add(new JScrollPane(completedPanel));

        mainPanel.add(centerPanel, BorderLayout.CENTER);
        add(mainPanel);

        loadMissionsFromDatabase();
    }

    /* ================= LOAD MISSIONS ================= */
    private void loadMissionsFromDatabase() {

        assignedPanel.removeAll();
        completedPanel.removeAll();

        try (Connection con = DBConnection.getConnection()) {

        	String sql =
        	        "SELECT order_id, order_number, delivery_date, delivery_address, status " +
        	        "FROM orders " +
        	        "WHERE driver_email = ? " +
        	        "ORDER BY delivery_date";


            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, driverEmail);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {

                String missionNo = "Mission #" + rs.getString("order_number");
                String pickup = "Pickup: Warehouse:123 Main Street, Roeun, France 7600";
                String dropoff = "Dropoff: " + rs.getString("delivery_address");
                String dateText;
                boolean completed;

                if ("COMPLETED".equalsIgnoreCase(rs.getString("status"))) {
                    dateText = "Completed: " + rs.getDate("delivery_date");
                    completed = true;
                } else {
                    dateText = "Due: " + rs.getDate("delivery_date");
                    completed = false;
                }

                JPanel card = createMissionCard(
                        missionNo,
                        pickup,
                        dropoff,
                        dateText,
                        completed,
                        rs.getInt("order_id")
                );

                if (completed) {
                    completedPanel.add(card);
                } else {
                    assignedPanel.add(card);
                }
            }

            assignedPanel.revalidate();
            completedPanel.revalidate();
            assignedPanel.repaint();
            completedPanel.repaint();

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Failed to load missions",
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /* ================= MISSION CARD ================= */
    private JPanel createMissionCard(
            String missionNo,
            String pickup,
            String dropoff,
            String dateText,
            boolean completed,
            int orderId
    ) {
        JPanel card = new JPanel(new BorderLayout(10, 10));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 130));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));

        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setBackground(Color.WHITE);

        JLabel missionLabel = new JLabel(missionNo);
        missionLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));

        JLabel pickupLabel = new JLabel(pickup);
        JLabel dropLabel = new JLabel(dropoff);

        JLabel dateLabel = new JLabel(dateText);
        dateLabel.setForeground(
                completed ? new Color(0, 153, 0) : new Color(0, 102, 204)
        );

        textPanel.add(missionLabel);
        textPanel.add(Box.createVerticalStrut(5));
        textPanel.add(pickupLabel);
        textPanel.add(dropLabel);
        textPanel.add(Box.createVerticalStrut(5));
        textPanel.add(dateLabel);

        JButton actionBtn = new JButton(
                completed ? "Completed" : "Click to Complete"
        );

        actionBtn.setEnabled(!completed);
        actionBtn.setBackground(
                completed ? new Color(46, 204, 113) : new Color(52, 120, 246)
        );
        actionBtn.setForeground(Color.WHITE);

        if (!completed) {
            actionBtn.addActionListener(e -> markAsCompleted(orderId));
        }

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.setBackground(Color.WHITE);
        btnPanel.add(actionBtn);

        card.add(textPanel, BorderLayout.CENTER);
        card.add(btnPanel, BorderLayout.EAST);

        return card;
    }

    /* ================= MARK COMPLETED ================= */
    private void markAsCompleted(int orderId) {

        try (Connection con = DBConnection.getConnection()) {

            String sql = "UPDATE orders SET status='COMPLETED' WHERE order_id=?";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, orderId);
            ps.executeUpdate();

            JOptionPane.showMessageDialog(this,
                    "Mission completed successfully");

            loadMissionsFromDatabase();

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Failed to update mission",
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /* ================= SECTION PANEL ================= */
    private JPanel createSectionPanel(String titleText) {

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel title = new JLabel(titleText);
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        panel.add(title);
        panel.add(Box.createVerticalStrut(15));

        return panel;
    }
}
