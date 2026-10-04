package com.example.bankmanagement.config;

import com.example.bankmanagement.entity.*;
import com.example.bankmanagement.repository.BankAccountRepository;
import com.example.bankmanagement.repository.CustomerRepository;
import com.example.bankmanagement.repository.TransactionRepository;
import com.example.bankmanagement.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Creates demo data on first startup only (when the users table is empty).
 * Credentials are for LOCAL DEVELOPMENT / DEMO purposes only.
 */
@Component
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final BankAccountRepository bankAccountRepository;
    private final TransactionRepository transactionRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(UserRepository userRepository,
                      CustomerRepository customerRepository,
                      BankAccountRepository bankAccountRepository,
                      TransactionRepository transactionRepository,
                      PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.customerRepository = customerRepository;
        this.bankAccountRepository = bankAccountRepository;
        this.transactionRepository = transactionRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) {
            return; // already seeded
        }

        // ---- Admin (no customer account needed) ----
        User admin = new User("admin", passwordEncoder.encode("admin123"), Role.ADMIN, null, true);
        userRepository.save(admin);

        // ---- Customer 1: Rahul ----
        Customer rahul = new Customer("CUST001", "Rahul", "Sharma",
                "rahul@example.com", "9876543210", "Bangalore", LocalDate.of(1995, 5, 12));
        customerRepository.save(rahul);
        userRepository.save(new User("rahul", passwordEncoder.encode("rahul123"), Role.CUSTOMER, rahul, true));

        BankAccount rahulAccount = new BankAccount("1000010001", rahul, AccountType.SAVINGS, new BigDecimal("50000"));
        bankAccountRepository.save(rahulAccount);

        transactionRepository.save(new Transaction("TXN00001", rahulAccount, TransactionType.DEPOSIT,
                new BigDecimal("50000"), new BigDecimal("50000"), "Initial deposit", null, "CREDIT"));
        transactionRepository.save(new Transaction("TXN00002", rahulAccount, TransactionType.DEPOSIT,
                new BigDecimal("5000"), new BigDecimal("55000"), "Salary", null, "CREDIT"));
        transactionRepository.save(new Transaction("TXN00003", rahulAccount, TransactionType.WITHDRAW,
                new BigDecimal("5000"), new BigDecimal("50000"), "ATM withdrawal", null, "DEBIT"));

        // ---- Customer 2: Amit ----
        Customer amit = new Customer("CUST002", "Amit", "Patel",
                "amit@example.com", "9876501234", "Mumbai", LocalDate.of(1990, 11, 3));
        customerRepository.save(amit);
        userRepository.save(new User("amit", passwordEncoder.encode("amit123"), Role.CUSTOMER, amit, true));

        BankAccount amitAccount = new BankAccount("1000010002", amit, AccountType.SAVINGS, new BigDecimal("30000"));
        bankAccountRepository.save(amitAccount);

        transactionRepository.save(new Transaction("TXN00004", amitAccount, TransactionType.DEPOSIT,
                new BigDecimal("30000"), new BigDecimal("30000"), "Initial deposit", null, "CREDIT"));
        transactionRepository.save(new Transaction("TXN00005", amitAccount, TransactionType.WITHDRAW,
                new BigDecimal("1000"), new BigDecimal("29000"), "ATM withdrawal", null, "DEBIT"));
        transactionRepository.save(new Transaction("TXN00006", amitAccount, TransactionType.DEPOSIT,
                new BigDecimal("1000"), new BigDecimal("30000"), "Cash deposit", null, "CREDIT"));

        System.out.println("Demo data created: admin/admin123, rahul/rahul123 (1000010001), amit/amit123 (1000010002)");
    }
}
