package com.example.bankmanagement.controller;

import com.example.bankmanagement.dto.CustomerResponse;
import com.example.bankmanagement.dto.UpdateProfileRequest;
import com.example.bankmanagement.service.CustomerService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/customers")
@PreAuthorize("hasRole('CUSTOMER')")
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @GetMapping("/profile")
    public ResponseEntity<CustomerResponse> getProfile(Authentication authentication) {
        return ResponseEntity.ok(customerService.getProfile(authentication.getName()));
    }

    @PutMapping("/profile")
    public ResponseEntity<CustomerResponse> updateProfile(Authentication authentication,
                                                          @Valid @RequestBody UpdateProfileRequest request) {
        return ResponseEntity.ok(customerService.updateProfile(authentication.getName(), request));
    }
}
