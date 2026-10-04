package com.example.bankmanagement.controller;

import com.example.bankmanagement.dto.*;
import com.example.bankmanagement.service.AccountService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping("/my-account")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<AccountResponse> myAccount(Authentication authentication) {
        return ResponseEntity.ok(accountService.getMyAccountResponse(authentication.getName()));
    }

    @GetMapping("/{accountNumber}")
    @PreAuthorize("hasAnyRole('CUSTOMER','ADMIN')")
    public ResponseEntity<AccountResponse> getAccount(@PathVariable String accountNumber,
                                                      Authentication authentication) {
        return ResponseEntity.ok(accountService.getAccountByNumber(accountNumber));
    }

    @PostMapping("/deposit")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<TransactionResponse> deposit(Authentication authentication,
                                                       @Valid @RequestBody DepositRequest request) {
        return ResponseEntity.ok(accountService.deposit(authentication.getName(), request));
    }

    @PostMapping("/withdraw")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<TransactionResponse> withdraw(Authentication authentication,
                                                        @Valid @RequestBody WithdrawRequest request) {
        return ResponseEntity.ok(accountService.withdraw(authentication.getName(), request));
    }

    @PostMapping("/transfer")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<TransactionResponse> transfer(Authentication authentication,
                                                        @Valid @RequestBody TransferRequest request) {
        return ResponseEntity.ok(accountService.transfer(authentication.getName(), request));
    }
}
