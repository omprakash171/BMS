package com.example.bankmanagement.dto;

import java.util.List;

public class CustomerDetailResponse {

    private CustomerResponse customer;
    private List<AccountResponse> accounts;

    public CustomerDetailResponse(CustomerResponse customer, List<AccountResponse> accounts) {
        this.customer = customer;
        this.accounts = accounts;
    }

    public CustomerResponse getCustomer() {
        return customer;
    }

    public List<AccountResponse> getAccounts() {
        return accounts;
    }
}
