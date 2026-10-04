package com.example.bankmanagement.repository;

import com.example.bankmanagement.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    Optional<Transaction> findByTransactionId(String transactionId);

    List<Transaction> findByAccount_AccountNumberOrderByTransactionDateDesc(String accountNumber);

    List<Transaction> findByAccount_AccountNumberAndTransactionTypeOrderByTransactionDateDesc(
            String accountNumber, com.example.bankmanagement.entity.TransactionType type);

    List<Transaction> findTop10ByOrderByTransactionDateDesc();
}
