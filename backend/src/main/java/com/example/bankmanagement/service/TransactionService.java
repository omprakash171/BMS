package com.example.bankmanagement.service;

import com.example.bankmanagement.dto.TransactionResponse;
import com.example.bankmanagement.entity.BankAccount;
import com.example.bankmanagement.entity.Customer;
import com.example.bankmanagement.entity.Transaction;
import com.example.bankmanagement.entity.TransactionType;
import com.example.bankmanagement.exception.ResourceNotFoundException;
import com.example.bankmanagement.repository.TransactionRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountService accountService;

    public TransactionService(TransactionRepository transactionRepository, AccountService accountService) {
        this.transactionRepository = transactionRepository;
        this.accountService = accountService;
    }

    /** Customer's transaction history across all of their accounts, newest first, with optional filters. */
    public List<TransactionResponse> getHistory(String username, String type, String status,
                                                LocalDate startDate, LocalDate endDate) {
        Customer customer = accountService.myAccount(username).getCustomer();
        List<BankAccount> accounts = accountService.getAccountsOfCustomer(customer);

        List<Transaction> transactions = accounts.stream()
                .flatMap(account -> transactionRepository
                        .findByAccount_AccountNumberOrderByTransactionDateDesc(account.getAccountNumber()).stream())
                .filter(t -> type == null || t.getTransactionType() == TransactionType.valueOf(type))
                .filter(t -> status == null || t.getStatus().name().equals(status))
                .filter(t -> startDate == null || !t.getTransactionDate().toLocalDate().isBefore(startDate))
                .filter(t -> endDate == null || !t.getTransactionDate().toLocalDate().isAfter(endDate))
                .sorted(Comparator.comparing(Transaction::getTransactionDate).reversed())
                .collect(Collectors.toList());

        return transactions.stream().map(TransactionResponse::new).collect(Collectors.toList());
    }

    /** A single transaction - customers may only see their own; admins may see any. */
    public TransactionResponse getTransactionDetail(String username, String role, String transactionId) {
        Transaction transaction = transactionRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found: " + transactionId));

        boolean isAdmin = "ADMIN".equals(role);
        if (!isAdmin) {
            Customer customer = accountService.myAccount(username).getCustomer();
            Long ownerId = transaction.getAccount().getCustomer().getId();
            if (!ownerId.equals(customer.getId())) {
                throw new ResourceNotFoundException("Transaction not found: " + transactionId);
            }
        }
        return new TransactionResponse(transaction);
    }

    public List<TransactionResponse> getRecentTransactions(int limit) {
        return transactionRepository.findTop10ByOrderByTransactionDateDesc().stream()
                .limit(limit)
                .map(TransactionResponse::new)
                .collect(Collectors.toList());
    }
}
