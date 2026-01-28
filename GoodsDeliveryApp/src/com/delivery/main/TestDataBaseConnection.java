package com.delivery.main;

import java.sql.Connection;

import com.delivery.util.DBConnection;

public class TestDataBaseConnection 
{
	public static void main(String[] args) {
        try (Connection con = DBConnection.getConnection()) 
        {
            System.out.println(" Database connected successfully!");
        } 
        catch (Exception e) 
        {
            e.printStackTrace();
        }
    }

}
