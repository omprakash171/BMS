package com.example.bankmanagement.controller;

import com.example.bankmanagement.dto.TransactionResponse;
import com.example.bankmanagement.service.TransactionService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    /** Transaction history for the logged-in customer, with optional filters. */
    @GetMapping
    public ResponseEntity<List<TransactionResponse>> getHistory(
            Authentication authentication,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        return ResponseEntity.ok(
                transactionService.getHistory(authentication.getName(), type, status, startDate, endDate));
    }

    @GetMapping("/{transactionId}")
    public ResponseEntity<TransactionResponse> getDetail(@PathVariable String transactionId,
                                                         Authentication authentication) {
        String role = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")) ? "ADMIN" : "CUSTOMER";
        return ResponseEntity.ok(
                transactionService.getTransactionDetail(authentication.getName(), role, transactionId));
    }
}
