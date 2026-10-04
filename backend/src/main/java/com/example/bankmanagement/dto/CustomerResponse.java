package com.example.bankmanagement.dto;

import com.example.bankmanagement.entity.Customer;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class CustomerResponse {

    private Long id;
    private String customerId;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String address;
    private LocalDate dateOfBirth;
    private LocalDateTime createdAt;
    private String status;

    public CustomerResponse(Customer customer) {
        this.id = customer.getId();
        this.customerId = customer.getCustomerId();
        this.firstName = customer.getFirstName();
        this.lastName = customer.getLastName();
        this.email = customer.getEmail();
        this.phone = customer.getPhone();
        this.address = customer.getAddress();
        this.dateOfBirth = customer.getDateOfBirth();
        this.createdAt = customer.getCreatedAt();
        this.status = customer.getStatus().name();
    }

    public Long getId() {
        return id;
    }

    public String getCustomerId() {
        return customerId;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public String getAddress() {
        return address;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public String getStatus() {
        return status;
    }
}
