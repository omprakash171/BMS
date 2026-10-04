package com.example.bankmanagement.dto;

public class CreateCustomerResult {

    private final String customerId;
    private final String accountNumber;
    private final String username;

    public CreateCustomerResult(String customerId, String accountNumber, String username) {
        this.customerId = customerId;
        this.accountNumber = accountNumber;
        this.username = username;
    }

    public String getCustomerId() {
        return customerId;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public String getUsername() {
        return username;
    }
}
