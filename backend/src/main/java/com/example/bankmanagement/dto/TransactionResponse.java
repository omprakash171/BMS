package com.example.bankmanagement.dto;

import com.example.bankmanagement.entity.Transaction;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TransactionResponse {

    private String transactionId;
    private String accountNumber;
    private String transactionType;
    private BigDecimal amount;
    private BigDecimal balanceAfterTransaction;
    private String description;
    private String referenceAccount;
    private LocalDateTime transactionDate;
    private String status;
    private String direction;

    public TransactionResponse(Transaction transaction) {
        this.transactionId = transaction.getTransactionId();
        this.accountNumber = transaction.getAccount().getAccountNumber();
        this.transactionType = transaction.getTransactionType().name();
        this.amount = transaction.getAmount();
        this.balanceAfterTransaction = transaction.getBalanceAfterTransaction();
        this.description = transaction.getDescription();
        this.referenceAccount = transaction.getReferenceAccount();
        this.transactionDate = transaction.getTransactionDate();
        this.status = transaction.getStatus().name();
        this.direction = transaction.getDirection();
    }

    public String getTransactionId() {
        return transactionId;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public String getTransactionType() {
        return transactionType;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public BigDecimal getBalanceAfterTransaction() {
        return balanceAfterTransaction;
    }

    public String getDescription() {
        return description;
    }

    public String getReferenceAccount() {
        return referenceAccount;
    }

    public LocalDateTime getTransactionDate() {
        return transactionDate;
    }

    public String getStatus() {
        return status;
    }

    public String getDirection() {
        return direction;
    }
}
