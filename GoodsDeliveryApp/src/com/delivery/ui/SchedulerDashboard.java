package com.delivery.ui;

import com.delivery.util.DBConnection;
import org.apache.poi.xwpf.usermodel.*;

import javax.swing.*;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Font;
import java.awt.Desktop;

import java.io.File;
import java.io.FileOutputStream;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import java.util.Date;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.Calendar;
import java.util.List;
import java.util.ArrayList;

public class SchedulerDashboard extends JFrame {

    private DefaultListModel<OrderItem> allOrdersModel = new DefaultListModel<>();
    private DefaultListModel<OrderItem> selectedOrdersModel = new DefaultListModel<>();

    private JList<OrderItem> allOrdersList;
    private JList<OrderItem> selectedOrdersList;

    private JComboBox<String> driverCombo;
    private JLabel capacityLabel = new JLabel("Capacity Used: 0 / 0 kg");

    private Map<String, String> driverEmailMap = new LinkedHashMap<>();
    private Map<String, Double> driverCapacityMap = new LinkedHashMap<>();

    private JSpinner assignDateSpinner;
    private JSpinner filterDateSpinner;

    private JTextField warehouseField;

    static class OrderItem {
        String orderNumber;
        double weight;

        OrderItem(String orderNumber, double weight) {
            this.orderNumber = orderNumber;
            this.weight = weight;
        }

        @Override
        public String toString() {
            return orderNumber + " | " + weight + " kg";
        }
    }

    public SchedulerDashboard(String email) {

        setTitle("Scheduler Dashboard");
        setSize(1100, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel main = new JPanel(new BorderLayout(15, 15));
        main.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        /* ================= TOP BAR ================= */

        JPanel topBar = new JPanel(new BorderLayout(10, 10));

        JPanel leftTop = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        leftTop.add(new JLabel("Warehouse:"));

        warehouseField = new JTextField("123 Main Street, Roeun, France 7600", 35);
        warehouseField.setEditable(false);
        leftTop.add(warehouseField);

        JLabel title = new JLabel("Delivery Assignments", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));

        JPanel rightTop = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));

        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.addActionListener(e -> refreshDashboard());

        JButton logoutBtn = new JButton("Logout");
        logoutBtn.addActionListener(e -> {
            new LoginFrame().setVisible(true);
            dispose();
        });

        rightTop.add(refreshBtn);
        rightTop.add(logoutBtn);

        topBar.add(leftTop, BorderLayout.WEST);
        topBar.add(title, BorderLayout.CENTER);
        topBar.add(rightTop, BorderLayout.EAST);

        main.add(topBar, BorderLayout.NORTH);

        /* ================= CENTER ================= */

        JPanel center = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.BOTH;

        gbc.gridx = 0;
        gbc.weightx = 0.4;
        gbc.weighty = 1;

        filterDateSpinner = new JSpinner(
                new SpinnerDateModel(new Date(), null, null, Calendar.DAY_OF_MONTH)
        );
        filterDateSpinner.setEditor(new JSpinner.DateEditor(filterDateSpinner, "dd-MM-yyyy"));
        filterDateSpinner.addChangeListener(e -> loadOrders());

        JPanel left = new JPanel(new BorderLayout(5, 5));
        left.add(filterDateSpinner, BorderLayout.NORTH);

        allOrdersList = new JList<>(allOrdersModel);
        left.add(new JScrollPane(allOrdersList), BorderLayout.CENTER);

        center.add(left, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.1;

        JButton addBtn = new JButton(">>");
        JButton removeBtn = new JButton("<<");

        JPanel mid = new JPanel();
        mid.setLayout(new BoxLayout(mid, BoxLayout.Y_AXIS));
        mid.add(addBtn);
        mid.add(removeBtn);

        center.add(mid, gbc);

        gbc.gridx = 2;
        gbc.weightx = 0.5;

        JPanel right = new JPanel(new GridBagLayout());
        GridBagConstraints r = new GridBagConstraints();
        r.insets = new Insets(6, 6, 6, 6);
        r.fill = GridBagConstraints.HORIZONTAL;
        r.gridx = 0;

        r.gridy = 0;
        right.add(new JLabel("Select Driver"), r);

        r.gridy++;
        driverCombo = new JComboBox<>();
        driverCombo.addActionListener(e -> updateCapacityLabel());
        right.add(driverCombo, r);

        r.gridy++;
        right.add(capacityLabel, r);

        r.gridy++;
        r.weighty = 1;
        r.fill = GridBagConstraints.BOTH;

        selectedOrdersList = new JList<>(selectedOrdersModel);
        right.add(new JScrollPane(selectedOrdersList), r);

        r.weighty = 0;
        r.fill = GridBagConstraints.HORIZONTAL;

        r.gridy++;
        JButton upBtn = new JButton("▲");
        JButton downBtn = new JButton("▼");

        JPanel movePanel = new JPanel();
        movePanel.add(upBtn);
        movePanel.add(downBtn);
        right.add(movePanel, r);

        r.gridy++;
        JButton assignBtn = new JButton("Assign Route");
        right.add(assignBtn, r);

        r.gridy++;
        assignDateSpinner = new JSpinner(
                new SpinnerDateModel(new Date(), null, null, Calendar.DAY_OF_MONTH)
        );
        assignDateSpinner.setEditor(new JSpinner.DateEditor(assignDateSpinner, "dd-MM-yyyy"));
        right.add(assignDateSpinner, r);

        r.gridy++;
        JButton wordBtn = new JButton("Generate Word File");
        right.add(wordBtn, r);

        center.add(right, gbc);
        main.add(center, BorderLayout.CENTER);
        add(main);

        addBtn.addActionListener(e -> addOrder());
        removeBtn.addActionListener(e -> removeOrder());
        upBtn.addActionListener(e -> movePriority(-1));
        downBtn.addActionListener(e -> movePriority(1));
        assignBtn.addActionListener(e -> assignOrders());
        wordBtn.addActionListener(e -> generateWordFile());

        loadDrivers();
        loadOrders();
    }

    private void refreshDashboard() {
        selectedOrdersModel.clear();
        allOrdersModel.clear();
        loadDrivers();
        loadOrders();
        updateCapacityLabel();
    }

    private void loadDrivers() {

        driverCombo.removeAllItems();
        driverEmailMap.clear();
        driverCapacityMap.clear();

        driverCombo.addItem("-- Select Driver --");

        try (Connection con = DBConnection.getConnection()) {

            ResultSet rs = con.prepareStatement(
                    "SELECT first_name, last_name, email, truck_capacity FROM users WHERE role='Driver'"
            ).executeQuery();

            while (rs.next()) {
                String label = rs.getString(1) + " " + rs.getString(2);
                driverCombo.addItem(label);
                driverEmailMap.put(label, rs.getString(3));
                driverCapacityMap.put(label, rs.getDouble(4));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadOrders() {

        allOrdersModel.clear();
        Date date = (Date) filterDateSpinner.getValue();

        try (Connection con = DBConnection.getConnection()) {

            String sql =
                    "SELECT o.order_number, " +
                    "SUM(CAST(oi.quantity AS DECIMAL(10,2))) AS weight " +
                    "FROM orders o " +
                    "JOIN order_items oi ON o.order_id = oi.order_id " +
                    "WHERE o.driver_email IS NULL " +
                    "AND o.delivery_date = ? " +
                    "GROUP BY o.order_number";

            PreparedStatement ps = con.prepareStatement(sql);
            ps.setDate(1, new java.sql.Date(date.getTime()));

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                allOrdersModel.addElement(
                        new OrderItem(
                                rs.getString("order_number"),
                                rs.getDouble("weight")
                        )
                );
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addOrder() {

        OrderItem item = allOrdersList.getSelectedValue();
        if (item == null) return;

        double capacity = getDriverCapacity();
        double newWeight = getSelectedWeight() + item.weight;

        if (newWeight > capacity) {
            JOptionPane.showMessageDialog(this,
                    "Truck capacity exceeded!\nMax: " + capacity + " kg");
            return;
        }

        // ✅ MOVE order (not copy)
        allOrdersModel.removeElement(item);
        selectedOrdersModel.addElement(item);

        updateCapacityLabel();
    }


    private void removeOrder() {

        OrderItem item = selectedOrdersList.getSelectedValue();
        if (item == null) return;

        // ✅ MOVE order back
        selectedOrdersModel.removeElement(item);
        allOrdersModel.addElement(item);

        updateCapacityLabel();
    }


    private void movePriority(int dir) {
        int i = selectedOrdersList.getSelectedIndex();
        if (i < 0) return;

        int ni = i + dir;
        if (ni < 0 || ni >= selectedOrdersModel.size()) return;

        OrderItem o = selectedOrdersModel.get(i);
        selectedOrdersModel.remove(i);
        selectedOrdersModel.add(ni, o);
        selectedOrdersList.setSelectedIndex(ni);
    }

    private void assignOrders() {

        if (driverCombo.getSelectedIndex() == 0 || selectedOrdersModel.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Select driver and orders");
            return;
        }

        try (Connection con = DBConnection.getConnection()) {

            PreparedStatement ps = con.prepareStatement(
                    "UPDATE orders SET driver_email=?, priority=?, delivery_date=?, status='ASSIGNED' WHERE order_number=?"
            );

            String email = driverEmailMap.get(driverCombo.getSelectedItem());
            Date date = (Date) assignDateSpinner.getValue();

            for (int i = 0; i < selectedOrdersModel.size(); i++) {
                ps.setString(1, email);
                ps.setInt(2, i + 1);
                ps.setDate(3, new java.sql.Date(date.getTime()));
                ps.setString(4, selectedOrdersModel.get(i).orderNumber);
                ps.addBatch();
            }

            ps.executeBatch();
            JOptionPane.showMessageDialog(this, "Orders Assigned");

            selectedOrdersModel.clear();
            updateCapacityLabel();
            loadOrders();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void generateWordFile() {

        Date selectedDate = (Date) assignDateSpinner.getValue();
        String warehouseAddress = warehouseField.getText();

        try (Connection con = DBConnection.getConnection()) {

            String sql =
                "SELECT " +
                "u.first_name, u.last_name, o.priority, o.delivery_address, o.delivery_date, " +
                "GROUP_CONCAT(p.name ORDER BY p.name SEPARATOR ', ') AS products " +
                "FROM orders o " +
                "JOIN users u ON o.driver_email = u.email " +
                "JOIN order_items oi ON o.order_id = oi.order_id " +
                "JOIN products p ON oi.product_id = p.id " +
                "WHERE DATE(o.delivery_date) = ? " +
                "AND UPPER(o.status) NOT IN ('NOT DELIVERED', 'PENDING', 'CANCELLED') " +
                "GROUP BY u.first_name, u.last_name, o.priority, o.delivery_address, o.delivery_date " +
                "ORDER BY u.first_name, o.priority";

            PreparedStatement ps = con.prepareStatement(sql);
            ps.setDate(1, new java.sql.Date(selectedDate.getTime()));

            ResultSet rs = ps.executeQuery();

            // ✅ Check if data exists
            if (!rs.isBeforeFirst()) {
                JOptionPane.showMessageDialog(
                    this,
                    "No assigned or completed deliveries found for selected date.",
                    "No Data",
                    JOptionPane.WARNING_MESSAGE
                );
                return;
            }

            XWPFDocument doc = new XWPFDocument();

            String currentDriver = null;
            XWPFTable table = null;
            int slNo = 1;
            Date missionDate = null;

            while (rs.next()) {

                if (missionDate == null) {
                    missionDate = rs.getDate("delivery_date");

                    XWPFParagraph title = doc.createParagraph();
                    title.setAlignment(ParagraphAlignment.CENTER);

                    XWPFRun titleRun = title.createRun();
                    titleRun.setBold(true);
                    titleRun.setFontSize(16);
                    titleRun.setText("Daily Delivery Missions - " + missionDate);
                }

                String driver =
                    rs.getString("first_name") + " " + rs.getString("last_name");

                if (!driver.equals(currentDriver)) {

                    currentDriver = driver;
                    slNo = 1; // ✅ RESET SL NO PER DRIVER

                    XWPFParagraph driverPara = doc.createParagraph();
                    XWPFRun driverRun = driverPara.createRun();
                    driverRun.setBold(true);
                    driverRun.setFontSize(12);
                    driverRun.setText("Driver: " + driver);

                    table = doc.createTable(1, 6);
                    table.getRow(0).getCell(0).setText("Sl No");
                    table.getRow(0).getCell(1).setText("Warehouse");
                    table.getRow(0).getCell(2).setText("Delivery Address");
                    table.getRow(0).getCell(3).setText("Products");
                    table.getRow(0).getCell(4).setText("Delivery Date");
                    table.getRow(0).getCell(5).setText("Priority");
                }

                XWPFTableRow row = table.createRow();
                row.getCell(0).setText(String.valueOf(slNo++));
                row.getCell(1).setText(warehouseAddress);
                row.getCell(2).setText(rs.getString("delivery_address"));
                row.getCell(3).setText(rs.getString("products"));
                row.getCell(4).setText(rs.getDate("delivery_date").toString());
                row.getCell(5).setText(String.valueOf(rs.getInt("priority")));
            }

            JFileChooser chooser = new JFileChooser();
            String fileName = "Daily_Delivery_Missions_" + System.currentTimeMillis() + ".docx";
            chooser.setSelectedFile(new File(fileName));

            if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;

            File file = chooser.getSelectedFile();

            try (FileOutputStream fos = new FileOutputStream(file)) {
                doc.write(fos);
            }

            doc.close();

            Desktop.getDesktop().open(file.getParentFile());

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, e.getMessage());
        }
    }







    private double getSelectedWeight() {
        double total = 0;
        for (int i = 0; i < selectedOrdersModel.size(); i++) {
            total += selectedOrdersModel.get(i).weight;
        }
        return total;
    }

    private double getDriverCapacity() {
        if (driverCombo.getSelectedIndex() <= 0) return 0;
        return driverCapacityMap.get(driverCombo.getSelectedItem());
    }

    private void updateCapacityLabel() {
        capacityLabel.setText(
                "Capacity Used: " + getSelectedWeight() + " / " + getDriverCapacity() + " kg"
        );
    }
}
