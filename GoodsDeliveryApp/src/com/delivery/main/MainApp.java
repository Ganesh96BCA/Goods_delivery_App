package com.delivery.main;

import javax.swing.SwingUtilities;

import com.delivery.ui.LoginFrame;

public class MainApp 
{
	 public static void main(String[] args) 
	 {
		 SwingUtilities.invokeLater(() -> {
	            new LoginFrame().setVisible(true);
	        });
	 }

}
