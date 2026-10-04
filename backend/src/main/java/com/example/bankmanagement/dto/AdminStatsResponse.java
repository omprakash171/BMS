package com.example.bankmanagement.dto;

import java.util.List;

public class AdminStatsResponse {

    private long totalCustomers;
    private long totalAccounts;
    private long activeAccounts;
    private long totalTransactions;
    private List<TransactionResponse> recentTransactions;

    public AdminStatsResponse(long totalCustomers, long totalAccounts, long activeAccounts,
                              long totalTransactions, List<TransactionResponse> recentTransactions) {
        this.totalCustomers = totalCustomers;
        this.totalAccounts = totalAccounts;
        this.activeAccounts = activeAccounts;
        this.totalTransactions = totalTransactions;
        this.recentTransactions = recentTransactions;
    }

    public long getTotalCustomers() {
        return totalCustomers;
    }

    public long getTotalAccounts() {
        return totalAccounts;
    }

    public long getActiveAccounts() {
        return activeAccounts;
    }

    public long getTotalTransactions() {
        return totalTransactions;
    }

    public List<TransactionResponse> getRecentTransactions() {
        return recentTransactions;
    }
}
