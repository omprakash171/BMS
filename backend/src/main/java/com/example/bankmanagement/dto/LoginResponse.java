package com.example.bankmanagement.dto;

public class LoginResponse {

    private String token;
    private String type = "Bearer";
    private String username;
    private String role;
    private String customerId;
    private String message;

    public LoginResponse(String token, String username, String role, String customerId, String message) {
        this.token = token;
        this.username = username;
        this.role = role;
        this.customerId = customerId;
        this.message = message;
    }

    public String getToken() {
        return token;
    }

    public String getType() {
        return type;
    }

    public String getUsername() {
        return username;
    }

    public String getRole() {
        return role;
    }

    public String getCustomerId() {
        return customerId;
    }

    public String getMessage() {
        return message;
    }
}
