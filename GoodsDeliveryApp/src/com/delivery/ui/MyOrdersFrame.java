package com.delivery.ui;

import com.delivery.util.DBConnection;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.sql.*;

public class MyOrdersFrame extends JFrame {

    private String email;
    private JTable table;

    public MyOrdersFrame(String email) {
        this.email = email;

        setTitle("My Orders");
        setSize(950, 450);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));
        mainPanel.setBackground(Color.WHITE);

        /* ================= TOP BAR ================= */
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(Color.WHITE);

        JLabel title = new JLabel("My Orders");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));

        JButton backBtn = new JButton("Back");
        JButton logoutBtn = new JButton("Logout");

        backBtn.addActionListener(e -> {
            new CustomerDashboard(email).setVisible(true);
            dispose();
        });

        logoutBtn.addActionListener(e -> {
            new LoginFrame().setVisible(true);
            dispose();
        });

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        rightPanel.setBackground(Color.WHITE);
        rightPanel.add(backBtn);
        rightPanel.add(logoutBtn);

        topPanel.add(title, BorderLayout.WEST);
        topPanel.add(rightPanel, BorderLayout.EAST);

        mainPanel.add(topPanel, BorderLayout.NORTH);

        /* ================= TABLE ================= */
        DefaultTableModel model = new DefaultTableModel(
                new String[]{"Order No", "Delivery Date", "Status", "View"}, 0
        ) {
            public boolean isCellEditable(int row, int col) {
                return col == 3;
            }
        };

        table = new JTable(model);
        table.setRowHeight(30);

        table.getColumn("View").setCellRenderer(new ButtonRenderer());
        table.getColumn("View").setCellEditor(new ButtonEditor(new JCheckBox()));

        loadOrders(model);

        mainPanel.add(new JScrollPane(table), BorderLayout.CENTER);
        add(mainPanel);
    }

    /* ================= LOAD ORDERS ================= */
    private void loadOrders(DefaultTableModel model) {

        try (Connection con = DBConnection.getConnection()) {

            String sql =
                    "SELECT order_number, delivery_date, status " +
                    "FROM orders WHERE customer_email=? " +
                    "ORDER BY delivery_date DESC";

            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, email);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getString("order_number"),
                        rs.getDate("delivery_date"),
                        rs.getString("status"),
                        "View"
                });
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /* ================= BUTTON RENDERER ================= */
    class ButtonRenderer extends JButton implements TableCellRenderer {
        public ButtonRenderer() {
            setText("View");
        }

        public Component getTableCellRendererComponent(
                JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int column) {
            return this;
        }
    }

    /* ================= BUTTON EDITOR ================= */
    class ButtonEditor extends DefaultCellEditor {

        private JButton button;
        private String orderNumber;

        public ButtonEditor(JCheckBox checkBox) {
            super(checkBox);
            button = new JButton("View");
            button.addActionListener(e -> showOrderItems(orderNumber));
        }

        public Component getTableCellEditorComponent(
                JTable table, Object value, boolean isSelected, int row, int column) {

            orderNumber = table.getValueAt(row, 0).toString();
            return button;
        }

        public Object getCellEditorValue() {
            return "View";
        }
    }

    /* ================= VIEW ORDER ITEMS ================= */
    private void showOrderItems(String orderNumber) {

        JDialog dialog = new JDialog(this, "Order Details - " + orderNumber, true);
        dialog.setSize(450, 300);
        dialog.setLocationRelativeTo(this);

        DefaultTableModel model = new DefaultTableModel(
                new String[]{"Product Name", "Quantity"}, 0
        ) {
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        JTable table = new JTable(model);
        table.setRowHeight(25);

        try (Connection con = DBConnection.getConnection()) {

            String sql =
                    "SELECT p.name, oi.quantity " +
                    "FROM order_items oi " +
                    "JOIN products p ON oi.product_id = p.id " +
                    "JOIN orders o ON oi.order_id = o.order_id " +
                    "WHERE o.order_number = ?";

            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, orderNumber);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getString("name"),
                        rs.getString("quantity")
                });
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        dialog.add(new JScrollPane(table));
        dialog.setVisible(true);
    }
}
