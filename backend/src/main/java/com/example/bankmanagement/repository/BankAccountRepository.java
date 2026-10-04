package com.example.bankmanagement.repository;

import com.example.bankmanagement.entity.BankAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BankAccountRepository extends JpaRepository<BankAccount, Long> {

    Optional<BankAccount> findByAccountNumber(String accountNumber);

    boolean existsByAccountNumber(String accountNumber);

    List<BankAccount> findByCustomer_Id(Long customerId);

    Optional<BankAccount> findFirstByCustomer_IdOrderByCreatedAtAsc(Long customerId);

    List<BankAccount> findByAccountNumberContainingIgnoreCaseOrCustomer_FirstNameContainingIgnoreCaseOrCustomer_LastNameContainingIgnoreCase(
            String accountNumber, String firstName, String lastName);
}
