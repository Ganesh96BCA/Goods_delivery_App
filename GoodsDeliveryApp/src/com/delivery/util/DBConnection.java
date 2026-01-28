package com.delivery.util;

import java.sql.Connection;
import java.sql.DriverManager;

public class DBConnection 
{
	private static final String URL ="jdbc:mysql://localhost:3306/GoodsDeliveryApp";
	    private static final String USER = "root";
	    private static final String PASSWORD = "Root@123";

	    public static Connection getConnection() throws Exception
	    {
	        return DriverManager.getConnection(URL, USER, PASSWORD);
	    }
	

}
