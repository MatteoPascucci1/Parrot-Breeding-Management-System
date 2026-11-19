package com.example.demo.payload;



public class LoginRequest { //I dati inviati dall'utente per fare login
	
	
	
    private String username;
    private String password;

    
    
    public String getUsername() { 
    	return username; 
    }
    
    public void setUsername(String username) { 
    	this.username = username; 
    }

    public String getPassword() { 
    	return password; 
    }
    
    public void setPassword(String password) { 
    	this.password = password; 
    }
    
    
    
}