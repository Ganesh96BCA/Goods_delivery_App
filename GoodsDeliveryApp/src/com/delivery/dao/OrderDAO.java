package com.delivery.dao;

import com.delivery.util.DBConnection;

import java.sql.*;
import java.util.*;

public class OrderDAO {

    public static int createOrder(
            String orderNumber,
            String email,
            java.sql.Date deliveryDate,
            String address
    ) throws Exception {

        String sql = "INSERT INTO orders (order_number, customer_email, delivery_date, delivery_address) VALUES (?, ?, ?, ?)";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, orderNumber);
            ps.setString(2, email);
            ps.setDate(3, deliveryDate);
            ps.setString(4, address);
            ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1); // order_id
            }
        }
        return -1;
    }

    public static void addOrderItem(int orderId, int productId, String qty) throws Exception {

        String sql = "INSERT INTO order_items (order_id, product_id, quantity) VALUES (?, ?, ?)";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, orderId);
            ps.setInt(2, productId);
            ps.setString(3, qty);
            ps.executeUpdate();
        }
    }
}
