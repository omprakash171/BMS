package com.example.bankmanagement.config;

import com.example.bankmanagement.repository.BankAccountRepository;
import com.example.bankmanagement.repository.CustomerRepository;
import com.example.bankmanagement.repository.TransactionRepository;
import org.springframework.stereotype.Component;

/**
 * Generates human-friendly unique IDs:
 *   customer IDs   -> CUST001, CUST002, ...
 *   account numbers -> 1000010001, 1000010002, ...
 *   transaction IDs -> TXN00001, TXN00002, ...
 */
@Component
public class IdGenerator {

    private static final long ACCOUNT_NUMBER_BASE = 1000010000L;

    private final CustomerRepository customerRepository;
    private final BankAccountRepository bankAccountRepository;
    private final TransactionRepository transactionRepository;

    public IdGenerator(CustomerRepository customerRepository,
                       BankAccountRepository bankAccountRepository,
                       TransactionRepository transactionRepository) {
        this.customerRepository = customerRepository;
        this.bankAccountRepository = bankAccountRepository;
        this.transactionRepository = transactionRepository;
    }

    public String nextCustomerId() {
        int n = (int) (customerRepository.count() + 1);
        String id;
        do {
            id = String.format("CUST%03d", n++);
        } while (customerRepository.findByCustomerId(id).isPresent());
        return id;
    }

    public String nextAccountNumber() {
        long n = ACCOUNT_NUMBER_BASE + bankAccountRepository.count() + 1;
        String number;
        do {
            number = String.valueOf(n++);
        } while (bankAccountRepository.existsByAccountNumber(number));
        return number;
    }

    public String nextTransactionId() {
        long n = transactionRepository.count() + 1;
        String id;
        do {
            id = String.format("TXN%05d", n++);
        } while (transactionRepository.findByTransactionId(id).isPresent());
        return id;
    }
}
