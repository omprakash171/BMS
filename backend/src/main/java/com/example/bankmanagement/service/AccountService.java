package com.example.bankmanagement.service;

import com.example.bankmanagement.config.IdGenerator;
import com.example.bankmanagement.dto.*;
import com.example.bankmanagement.entity.*;
import com.example.bankmanagement.exception.BadRequestException;
import com.example.bankmanagement.exception.InsufficientBalanceException;
import com.example.bankmanagement.exception.ResourceNotFoundException;
import com.example.bankmanagement.repository.BankAccountRepository;
import com.example.bankmanagement.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class AccountService {

    private final BankAccountRepository bankAccountRepository;
    private final TransactionRepository transactionRepository;
    private final CustomerService customerService;
    private final IdGenerator idGenerator;

    public AccountService(BankAccountRepository bankAccountRepository,
                          TransactionRepository transactionRepository,
                          CustomerService customerService,
                          IdGenerator idGenerator) {
        this.bankAccountRepository = bankAccountRepository;
        this.transactionRepository = transactionRepository;
        this.customerService = customerService;
        this.idGenerator = idGenerator;
    }

    /** The logged-in customer's primary (first) account. */
    public BankAccount myAccount(String username) {
        Customer customer = customerService.getCustomerByUsername(username);
        return bankAccountRepository.findFirstByCustomer_IdOrderByCreatedAtAsc(customer.getId())
                .orElseThrow(() -> new ResourceNotFoundException("No bank account found for this customer"));
    }

    public AccountResponse getMyAccountResponse(String username) {
        return new AccountResponse(myAccount(username));
    }

    public AccountResponse getAccountByNumber(String accountNumber) {
        BankAccount account = findByAccountNumber(accountNumber);
        return new AccountResponse(account);
    }

    public BankAccount findByAccountNumber(String accountNumber) {
        return bankAccountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found: " + accountNumber));
    }

    @Transactional
    public TransactionResponse deposit(String username, DepositRequest request) {
        BankAccount account = myAccount(username);
        ensureCustomerActive(account);
        ensureAccountActive(account);
        validateAmount(request.getAmount());

        BigDecimal newBalance = account.getBalance().add(request.getAmount());
        account.setBalance(newBalance);
        bankAccountRepository.save(account);

        Transaction transaction = new Transaction(
                idGenerator.nextTransactionId(),
                account,
                TransactionType.DEPOSIT,
                request.getAmount(),
                newBalance,
                request.getDescription(),
                null,
                "CREDIT"
        );
        transactionRepository.save(transaction);
        return new TransactionResponse(transaction);
    }

    @Transactional
    public TransactionResponse withdraw(String username, WithdrawRequest request) {
        BankAccount account = myAccount(username);
        ensureCustomerActive(account);
        ensureAccountActive(account);
        validateAmount(request.getAmount());

        if (account.getBalance().compareTo(request.getAmount()) < 0) {
            throw new InsufficientBalanceException("Insufficient balance");
        }

        BigDecimal newBalance = account.getBalance().subtract(request.getAmount());
        account.setBalance(newBalance);
        bankAccountRepository.save(account);

        Transaction transaction = new Transaction(
                idGenerator.nextTransactionId(),
                account,
                TransactionType.WITHDRAW,
                request.getAmount(),
                newBalance,
                request.getDescription(),
                null,
                "DEBIT"
        );
        transactionRepository.save(transaction);
        return new TransactionResponse(transaction);
    }

    @Transactional
    public TransactionResponse transfer(String username, TransferRequest request) {
        BankAccount sender = myAccount(username);

        BankAccount receiver = bankAccountRepository.findByAccountNumber(request.getToAccountNumber())
                .orElseThrow(() -> new ResourceNotFoundException("Receiver account not found"));

        if (sender.getAccountNumber().equals(receiver.getAccountNumber())) {
            throw new BadRequestException("Sender and receiver accounts must be different");
        }

        ensureCustomerActive(sender);
        ensureAccountActive(sender);
        ensureAccountActive(receiver);
        validateAmount(request.getAmount());

        if (sender.getBalance().compareTo(request.getAmount()) < 0) {
            throw new InsufficientBalanceException("Insufficient balance");
        }

        // Move the money (all-or-nothing thanks to @Transactional)
        BigDecimal senderNewBalance = sender.getBalance().subtract(request.getAmount());
        BigDecimal receiverNewBalance = receiver.getBalance().add(request.getAmount());
        sender.setBalance(senderNewBalance);
        receiver.setBalance(receiverNewBalance);
        bankAccountRepository.save(sender);
        bankAccountRepository.save(receiver);

        String description = (request.getDescription() == null || request.getDescription().isBlank())
                ? "Transfer"
                : request.getDescription();

        Transaction senderTransaction = new Transaction(
                idGenerator.nextTransactionId(),
                sender,
                TransactionType.TRANSFER,
                request.getAmount(),
                senderNewBalance,
                description,
                receiver.getAccountNumber(),
                "DEBIT"
        );
        transactionRepository.save(senderTransaction);

        Transaction receiverTransaction = new Transaction(
                idGenerator.nextTransactionId(),
                receiver,
                TransactionType.TRANSFER,
                request.getAmount(),
                receiverNewBalance,
                description,
                sender.getAccountNumber(),
                "CREDIT"
        );
        transactionRepository.save(receiverTransaction);

        return new TransactionResponse(senderTransaction);
    }

    public List<BankAccount> getAccountsOfCustomer(Customer customer) {
        return bankAccountRepository.findByCustomer_Id(customer.getId());
    }

    private void validateAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Amount must be greater than 0");
        }
    }

    private void ensureAccountActive(BankAccount account) {
        if (account.getStatus() == AccountStatus.BLOCKED) {
            throw new BadRequestException("Account is blocked");
        }
        if (account.getStatus() == AccountStatus.CLOSED) {
            throw new BadRequestException("Account is closed");
        }
    }

    private void ensureCustomerActive(BankAccount account) {
        if (account.getCustomer().getStatus() != CustomerStatus.ACTIVE) {
            throw new BadRequestException("Customer account is deactivated. Please contact the bank.");
        }
    }
}
