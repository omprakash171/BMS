package com.example.bankmanagement.dto;

import com.example.bankmanagement.entity.BankAccount;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class AccountResponse {

    private String accountNumber;
    private String accountType;
    private BigDecimal balance;
    private String status;
    private LocalDateTime createdAt;
    private String customerName;
    private String customerId;

    public AccountResponse(BankAccount account) {
        this.accountNumber = account.getAccountNumber();
        this.accountType = account.getAccountType().name();
        this.balance = account.getBalance();
        this.status = account.getStatus().name();
        this.createdAt = account.getCreatedAt();
        this.customerName = account.getCustomer().getFullName();
        this.customerId = account.getCustomer().getCustomerId();
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public String getAccountType() {
        return accountType;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public String getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public String getCustomerName() {
        return customerName;
    }

    public String getCustomerId() {
        return customerId;
    }
}
