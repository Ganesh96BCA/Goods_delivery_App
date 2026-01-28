package com.delivery.model;

public class User 
{
	 private int id;
	    private String email;
	    private String password;
	    private String phone;
	    private String role;
	    private String truckNumber;
	    private int truckCapacity;
	    
	    public int getId() {
	        return id;
	    }

	    public String getEmail() {
	        return email;
	    }

	    public String getPassword() {
	        return password;
	    }

	    public String getPhone() {
	        return phone;
	    }

	    public String getRole() {
	        return role;
	    }

	    public String getTruckNumber() {
	        return truckNumber;
	    }

	    public int getTruckCapacity() {
	        return truckCapacity;
	    }

	    // setters
	    public void setId(int id) {
	        this.id = id;
	    }

	    public void setEmail(String email) {
	        this.email = email;
	    }

	    public void setPassword(String password) {
	        this.password = password;
	    }

	    public void setPhone(String phone) {
	        this.phone = phone;
	    }

	    public void setRole(String role) {
	        this.role = role;
	    }

	    public void setTruckNumber(String truckNumber) {
	        this.truckNumber = truckNumber;
	    }

	    public void setTruckCapacity(int truckCapacity) {
	        this.truckCapacity = truckCapacity;
	    }

}
