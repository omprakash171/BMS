package com.example.bankmanagement.controller;

import com.example.bankmanagement.dto.*;
import com.example.bankmanagement.service.AdminService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/stats")
    public ResponseEntity<AdminStatsResponse> stats() {
        return ResponseEntity.ok(adminService.getStats());
    }

    @GetMapping("/customers")
    public ResponseEntity<List<CustomerResponse>> listCustomers(
            @RequestParam(required = false) String search) {
        return ResponseEntity.ok(adminService.listCustomers(search));
    }

    @GetMapping("/customers/{id}")
    public ResponseEntity<CustomerDetailResponse> getCustomer(@PathVariable Long id) {
        return ResponseEntity.ok(adminService.getCustomerDetail(id));
    }

    @PostMapping("/customers")
    public ResponseEntity<CreateCustomerResult> createCustomer(
            @Valid @RequestBody AdminCreateCustomerRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(adminService.createCustomer(request));
    }

    @PutMapping("/customers/{id}/status")
    public ResponseEntity<CustomerResponse> updateCustomerStatus(@PathVariable Long id,
                                                                 @Valid @RequestBody UpdateStatusRequest request) {
        return ResponseEntity.ok(adminService.updateCustomerStatus(id, request.getStatus()));
    }

    @GetMapping("/accounts")
    public ResponseEntity<List<AccountResponse>> listAccounts(
            @RequestParam(required = false) String search) {
        return ResponseEntity.ok(adminService.listAccounts(search));
    }

    @PutMapping("/accounts/{accountNumber}/status")
    public ResponseEntity<AccountResponse> updateAccountStatus(@PathVariable String accountNumber,
                                                               @Valid @RequestBody UpdateStatusRequest request) {
        return ResponseEntity.ok(adminService.updateAccountStatus(accountNumber, request.getStatus()));
    }

    @GetMapping("/transactions")
    public ResponseEntity<List<TransactionResponse>> listTransactions() {
        return ResponseEntity.ok(adminService.listAllTransactions());
    }
}
