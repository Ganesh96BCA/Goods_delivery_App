package com.delivery.ui;

import com.delivery.dao.OrderDAO;
import com.delivery.dao.ProductDAO;
import com.delivery.model.Product;

import javax.swing.*;
import java.awt.*;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class CustomerDashboard extends JFrame {

    private JPanel productsPanel;
    private JSpinner dateSpinner;
    private JTextArea addressArea;
    private String userEmail;

    public CustomerDashboard(String userEmail) {
        this.userEmail = userEmail;

        setTitle("Customer Dashboard - Order Products");
        setSize(1100, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        mainPanel.setBackground(Color.WHITE);

        
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(Color.WHITE);

        JLabel title = new JLabel("Order Products", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));

        JButton myOrdersBtn = new JButton("My Orders");
        myOrdersBtn.addActionListener(e -> {
            new MyOrdersFrame(userEmail).setVisible(true);
            dispose();
        });

        JButton logoutBtn = new JButton("Logout");
        logoutBtn.addActionListener(e -> {
            new LoginFrame().setVisible(true);
            dispose();
        });

        JPanel rightTop = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        rightTop.setBackground(Color.WHITE);
        rightTop.add(myOrdersBtn);
        rightTop.add(logoutBtn);

        topPanel.add(title, BorderLayout.CENTER);
        topPanel.add(rightTop, BorderLayout.EAST);
        mainPanel.add(topPanel, BorderLayout.NORTH);

        /* ================= CENTER ================= */
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setBackground(Color.WHITE);

        JLabel selectLabel = new JLabel("Select Products");
        selectLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        centerPanel.add(selectLabel);
        centerPanel.add(Box.createVerticalStrut(15));

        productsPanel = new JPanel(new GridLayout(0, 2, 20, 20));
        productsPanel.setBackground(Color.WHITE);

        loadProductsFromDatabase();

        JScrollPane productScroll = new JScrollPane(productsPanel);
        productScroll.setPreferredSize(new Dimension(1000, 350));
        productScroll.setBorder(null);

        centerPanel.add(productScroll);
        centerPanel.add(Box.createVerticalStrut(20));

        /* ================= DELIVERY DETAILS ================= */
        JLabel deliveryLabel = new JLabel("Delivery Details");
        deliveryLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        centerPanel.add(deliveryLabel);
        centerPanel.add(Box.createVerticalStrut(10));

        JPanel deliveryPanel = new JPanel(new GridLayout(2, 2, 15, 15));
        deliveryPanel.setBackground(Color.WHITE);

        deliveryPanel.add(new JLabel("Delivery Date"));

        SpinnerDateModel dateModel =
                new SpinnerDateModel(new Date(), null, null, Calendar.DAY_OF_MONTH);
        dateSpinner = new JSpinner(dateModel);
        dateSpinner.setEditor(new JSpinner.DateEditor(dateSpinner, "dd-MM-yyyy"));
        deliveryPanel.add(dateSpinner);

        deliveryPanel.add(new JLabel("Delivery Address"));

        addressArea = new JTextArea(3, 25);
        addressArea.setLineWrap(true);
        addressArea.setWrapStyleWord(true);
        addressArea.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        deliveryPanel.add(addressArea);

        centerPanel.add(deliveryPanel);
        centerPanel.add(Box.createVerticalStrut(25));

        /* ================= CONFIRM BUTTON ================= */
        JButton confirmBtn = new JButton("Confirm to place order");
        confirmBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        confirmBtn.setBackground(new Color(52, 120, 246));
        confirmBtn.setForeground(Color.WHITE);
        confirmBtn.setPreferredSize(new Dimension(240, 40));
        confirmBtn.addActionListener(e -> placeOrder());

        centerPanel.add(confirmBtn);
        mainPanel.add(centerPanel, BorderLayout.CENTER);
        add(mainPanel);
    }

    /* ================= LOAD PRODUCTS ================= */
    private void loadProductsFromDatabase() {
        List<Product> products = ProductDAO.getAllProducts();
        for (Product p : products) {
            productsPanel.add(createProductCard(p));
        }
    }

    /* ================= PRODUCT CARD ================= */
    private JPanel createProductCard(Product product) {

        JPanel card = new JPanel(new BorderLayout(10, 10));
        card.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        card.setBackground(Color.WHITE);
        card.putClientProperty("product", product);

        ImageIcon icon = new ImageIcon(product.getImagePath());
        Image img = icon.getImage().getScaledInstance(80, 80, Image.SCALE_SMOOTH);
        JLabel imageLabel = new JLabel(new ImageIcon(img));

        JLabel infoLabel = new JLabel(
                "<html><b>" + product.getName() + "</b><br/>" +
                        product.getCategory() + "</html>"
        );

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT));
        left.setBackground(Color.WHITE);
        left.add(imageLabel);
        left.add(infoLabel);

        JCheckBox selectBox = new JCheckBox("Select");
        JTextField qtyField = new JTextField("Kg", 5);
        qtyField.setEnabled(false);

        selectBox.addActionListener(e -> {
            if (selectBox.isSelected()) {
                qtyField.setText("");
                qtyField.setEnabled(true);
                qtyField.requestFocus();
            } else {
                qtyField.setText("Kg");
                qtyField.setEnabled(false);
            }
        });

        JPanel right = new JPanel();
        right.setBackground(Color.WHITE);
        right.add(selectBox);
        right.add(qtyField);

        card.add(left, BorderLayout.WEST);
        card.add(right, BorderLayout.EAST);

        return card;
    }

    /* ================= PLACE ORDER ================= */
    private void placeOrder() {

        Date selectedDate = (Date) dateSpinner.getValue();

        Calendar selectedCal = Calendar.getInstance();
        selectedCal.setTime(selectedDate);
        selectedCal.set(Calendar.HOUR_OF_DAY, 0);
        selectedCal.set(Calendar.MINUTE, 0);
        selectedCal.set(Calendar.SECOND, 0);

        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);

        if (!selectedCal.after(today)) {
            JOptionPane.showMessageDialog(this,
                    "Delivery date must be a future date",
                    "Invalid Date",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (addressArea.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please provide delivery address",
                    "Validation Error",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        boolean selected = false;
        for (Component c : productsPanel.getComponents()) {
            JPanel card = (JPanel) c;
            JCheckBox cb = (JCheckBox) ((JPanel) card.getComponent(1)).getComponent(0);
            if (cb.isSelected()) {
                selected = true;
                break;
            }
        }

        if (!selected) {
            JOptionPane.showMessageDialog(this,
                    "Please select at least one product",
                    "Validation Error",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            String orderNumber = "ORD-" + System.currentTimeMillis();
            java.sql.Date sqlDate = new java.sql.Date(selectedDate.getTime());

            int orderId = OrderDAO.createOrder(
                    orderNumber,
                    userEmail,
                    sqlDate,
                    addressArea.getText().trim()
            );

            for (Component c : productsPanel.getComponents()) {

                JPanel card = (JPanel) c;
                JPanel right = (JPanel) card.getComponent(1);
                JCheckBox cb = (JCheckBox) right.getComponent(0);
                JTextField qtyField = (JTextField) right.getComponent(1);

                if (cb.isSelected()) {

                	String qtyText = qtyField.getText().trim();

                	if (qtyText.isEmpty()) {
                	    JOptionPane.showMessageDialog(this,
                	            "Please enter quantity for selected products",
                	            "Validation Error",
                	            JOptionPane.WARNING_MESSAGE);
                	    return;
                	}

                	int quantity;

                	try {
                	    quantity = Integer.parseInt(qtyText);
                	    if (quantity <= 0) {
                	        throw new NumberFormatException();
                	    }
                	} catch (NumberFormatException ex) {
                	    JOptionPane.showMessageDialog(this,
                	            "Quantity must be a positive number",
                	            "Validation Error",
                	            JOptionPane.WARNING_MESSAGE);
                	    return;
                	}

                	Product p = (Product) card.getClientProperty("product");
                	OrderDAO.addOrderItem(orderId, p.getId(), String.valueOf(quantity));

                }
            }

            int result = JOptionPane.showConfirmDialog(
                    this,
                    "Order placed successfully!\nOrder No: " + orderNumber,
                    "Success",
                    JOptionPane.DEFAULT_OPTION,
                    JOptionPane.INFORMATION_MESSAGE
            );

            if (result == JOptionPane.OK_OPTION) {
                resetDashboard();
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Failed to place order",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /* ================= RESET DASHBOARD ================= */
    private void resetDashboard() {

        dateSpinner.setValue(new Date());
        addressArea.setText("");

        for (Component c : productsPanel.getComponents()) {

            JPanel card = (JPanel) c;
            JPanel right = (JPanel) card.getComponent(1);

            JCheckBox cb = (JCheckBox) right.getComponent(0);
            JTextField qtyField = (JTextField) right.getComponent(1);

            cb.setSelected(false);
            qtyField.setText("Kg");
            qtyField.setEnabled(false);
        }

        productsPanel.revalidate();
        productsPanel.repaint();
    }
}
