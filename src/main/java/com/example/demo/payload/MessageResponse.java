package com.example.demo.payload;



public class MessageResponse { //Per inviare messaggi di conferma o di errore in formato JSON
	
	
	
    private String message;
    
    

    public MessageResponse(String message) {
        this.message = message;
    }
    
    

    public String getMessage() { 
    	return message; 
    }
    
    public void setMessage(String message) { 
    	this.message = message; 
    }
    
    
    
}