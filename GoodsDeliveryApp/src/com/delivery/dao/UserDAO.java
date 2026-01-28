package com.delivery.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import com.delivery.model.User;
import com.delivery.util.DBConnection;

public class UserDAO 
{
	 public boolean register(User user) throws Exception {
	        String sql = "INSERT INTO users(email,password,phone,role,truck_number,truck_capacity) VALUES (?,?,?,?,?,?)";
	        try (Connection con = DBConnection.getConnection();
	             PreparedStatement ps = con.prepareStatement(sql)) {

	            ps.setString(1, user.getEmail());
	            ps.setString(2, user.getPassword());
	            ps.setString(3, user.getPhone());
	            ps.setString(4, user.getRole());
	            ps.setString(5, user.getTruckNumber());
	            ps.setInt(6, user.getTruckCapacity());

	            return ps.executeUpdate() > 0;
	        }

}
	 public User login(String email, String password) throws Exception {
	        String sql = "SELECT * FROM users WHERE email=? AND password=?";
	        try (Connection con = DBConnection.getConnection();
	             PreparedStatement ps = con.prepareStatement(sql)) {

	            ps.setString(1, email);
	            ps.setString(2, password);

	            ResultSet rs = ps.executeQuery();
	            if (rs.next()) {
	                User u = new User();
	                u.setId(rs.getInt("id"));
	                u.setEmail(rs.getString("email"));
	                u.setRole(rs.getString("role"));
	                return u;
	            }
	        }
	        return null;
	    }
}
