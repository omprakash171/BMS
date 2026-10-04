package com.example.bankmanagement.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public class RegisterRequest {

    @NotBlank(message = "is required")
    @Size(min = 3, max = 30, message = "must be between 3 and 30 characters")
    private String username;

    @NotBlank(message = "is required")
    @Size(min = 6, message = "must be at least 6 characters")
    private String password;

    @NotBlank(message = "is required")
    private String firstName;

    @NotBlank(message = "is required")
    private String lastName;

    @NotBlank(message = "is required")
    @Email(message = "must be a valid email")
    private String email;

    @NotBlank(message = "is required")
    private String phone;

    private String address;

    private LocalDate dateOfBirth;

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

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }
}
