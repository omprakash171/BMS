package com.example.bankmanagement.service;

import com.example.bankmanagement.config.IdGenerator;
import com.example.bankmanagement.dto.*;
import com.example.bankmanagement.entity.*;
import com.example.bankmanagement.exception.BadRequestException;
import com.example.bankmanagement.exception.ResourceNotFoundException;
import com.example.bankmanagement.repository.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AdminService {

    private final CustomerRepository customerRepository;
    private final UserRepository userRepository;
    private final BankAccountRepository bankAccountRepository;
    private final TransactionRepository transactionRepository;
    private final PasswordEncoder passwordEncoder;
    private final IdGenerator idGenerator;

    public AdminService(CustomerRepository customerRepository,
                        UserRepository userRepository,
                        BankAccountRepository bankAccountRepository,
                        TransactionRepository transactionRepository,
                        PasswordEncoder passwordEncoder,
                        IdGenerator idGenerator) {
        this.customerRepository = customerRepository;
        this.userRepository = userRepository;
        this.bankAccountRepository = bankAccountRepository;
        this.transactionRepository = transactionRepository;
        this.passwordEncoder = passwordEncoder;
        this.idGenerator = idGenerator;
    }

    public AdminStatsResponse getStats() {
        long totalCustomers = customerRepository.count();
        long totalAccounts = bankAccountRepository.count();
        long activeAccounts = bankAccountRepository.findAll().stream()
                .filter(account -> account.getStatus() == AccountStatus.ACTIVE)
                .count();
        long totalTransactions = transactionRepository.count();

        List<TransactionResponse> recent = transactionRepository.findTop10ByOrderByTransactionDateDesc().stream()
                .map(TransactionResponse::new)
                .collect(Collectors.toList());

        return new AdminStatsResponse(totalCustomers, totalAccounts, activeAccounts, totalTransactions, recent);
    }

    public List<CustomerResponse> listCustomers(String search) {
        List<Customer> customers;
        if (search == null || search.isBlank()) {
            customers = customerRepository.findAll();
        } else {
            customers = customerRepository
                    .findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCaseOrCustomerIdContainingIgnoreCaseOrEmailContainingIgnoreCase(
                            search, search, search, search);
        }
        return customers.stream().map(CustomerResponse::new).collect(Collectors.toList());
    }

    public Customer getCustomerById(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + id));
    }

    public CustomerResponse getCustomerResponse(Long id) {
        return new CustomerResponse(getCustomerById(id));
    }

    /** Customer detail together with their bank accounts. */
    public CustomerDetailResponse getCustomerDetail(Long id) {
        Customer customer = getCustomerById(id);
        List<AccountResponse> accounts = bankAccountRepository.findByCustomer_Id(customer.getId()).stream()
                .map(AccountResponse::new)
                .collect(Collectors.toList());
        return new CustomerDetailResponse(new CustomerResponse(customer), accounts);
    }

    @Transactional
    public CreateCustomerResult createCustomer(AdminCreateCustomerRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BadRequestException("Username is already taken");
        }
        if (customerRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new BadRequestException("Email is already in use");
        }

        AccountType accountType;
        try {
            accountType = AccountType.valueOf(request.getAccountType().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new BadRequestException("Account type must be SAVINGS or CURRENT");
        }

        BigDecimal initialDeposit = request.getInitialDeposit() != null ? request.getInitialDeposit() : BigDecimal.ZERO;
        if (initialDeposit.compareTo(BigDecimal.ZERO) < 0) {
            throw new BadRequestException("Initial deposit cannot be negative");
        }

        Customer customer = new Customer(
                idGenerator.nextCustomerId(),
                request.getFirstName(),
                request.getLastName(),
                request.getEmail(),
                request.getPhone(),
                request.getAddress(),
                request.getDateOfBirth()
        );
        customerRepository.save(customer);

        User user = new User(
                request.getUsername(),
                passwordEncoder.encode(request.getPassword()),
                Role.CUSTOMER,
                customer,
                true
        );
        userRepository.save(user);

        BankAccount account = new BankAccount(
                idGenerator.nextAccountNumber(),
                customer,
                accountType,
                initialDeposit
        );
        bankAccountRepository.save(account);

        if (initialDeposit.compareTo(BigDecimal.ZERO) > 0) {
            Transaction opening = new Transaction(
                    idGenerator.nextTransactionId(),
                    account,
                    TransactionType.DEPOSIT,
                    initialDeposit,
                    initialDeposit,
                    "Initial deposit",
                    null,
                    "CREDIT"
            );
            transactionRepository.save(opening);
        }

        return new CreateCustomerResult(customer.getCustomerId(), account.getAccountNumber(), user.getUsername());
    }

    @Transactional
    public CustomerResponse updateCustomerStatus(Long id, String status) {
        Customer customer = getCustomerById(id);

        CustomerStatus newStatus;
        try {
            newStatus = CustomerStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new BadRequestException("Status must be ACTIVE or INACTIVE");
        }
        customer.setStatus(newStatus);
        customerRepository.save(customer);

        // Deactivating a customer also disables their login
        userRepository.findByCustomer_Id(customer.getId()).ifPresent(user -> {
            user.setEnabled(newStatus == CustomerStatus.ACTIVE);
            userRepository.save(user);
        });

        return new CustomerResponse(customer);
    }

    public List<AccountResponse> listAccounts(String search) {
        List<BankAccount> accounts;
        if (search == null || search.isBlank()) {
            accounts = bankAccountRepository.findAll();
        } else {
            accounts = bankAccountRepository
                    .findByAccountNumberContainingIgnoreCaseOrCustomer_FirstNameContainingIgnoreCaseOrCustomer_LastNameContainingIgnoreCase(
                            search, search, search);
        }
        return accounts.stream().map(AccountResponse::new).collect(Collectors.toList());
    }

    @Transactional
    public AccountResponse updateAccountStatus(String accountNumber, String status) {
        BankAccount account = bankAccountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found: " + accountNumber));

        AccountStatus newStatus;
        try {
            newStatus = AccountStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new BadRequestException("Status must be ACTIVE, BLOCKED or CLOSED");
        }
        account.setStatus(newStatus);
        bankAccountRepository.save(account);
        return new AccountResponse(account);
    }

    public List<TransactionResponse> listAllTransactions() {
        return transactionRepository.findAll().stream()
                .sorted(java.util.Comparator.comparing(Transaction::getTransactionDate).reversed())
                .map(TransactionResponse::new)
                .collect(Collectors.toList());
    }
}
