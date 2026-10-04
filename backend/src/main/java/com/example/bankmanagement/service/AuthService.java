package com.example.bankmanagement.service;

import com.example.bankmanagement.config.IdGenerator;
import com.example.bankmanagement.dto.LoginRequest;
import com.example.bankmanagement.dto.LoginResponse;
import com.example.bankmanagement.dto.RegisterRequest;
import com.example.bankmanagement.entity.*;
import com.example.bankmanagement.exception.BadRequestException;
import com.example.bankmanagement.repository.BankAccountRepository;
import com.example.bankmanagement.repository.CustomerRepository;
import com.example.bankmanagement.repository.UserRepository;
import com.example.bankmanagement.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final BankAccountRepository bankAccountRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final IdGenerator idGenerator;

    public AuthService(AuthenticationManager authenticationManager,
                       UserRepository userRepository,
                       CustomerRepository customerRepository,
                       BankAccountRepository bankAccountRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService,
                       IdGenerator idGenerator) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.customerRepository = customerRepository;
        this.bankAccountRepository = bankAccountRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.idGenerator = idGenerator;
    }

    public LoginResponse login(LoginRequest request) {
        // Throws BadCredentialsException / DisabledException on failure
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new BadRequestException("User not found"));

        String token = jwtService.generateToken(user.getUsername(), user.getRole().name());
        String customerId = user.getCustomer() != null ? user.getCustomer().getCustomerId() : null;
        return new LoginResponse(token, user.getUsername(), user.getRole().name(), customerId, "Login successful");
    }

    @Transactional
    public LoginResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BadRequestException("Username is already taken");
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

        // Every new customer gets a SAVINGS account with zero balance
        BankAccount account = new BankAccount(
                idGenerator.nextAccountNumber(),
                customer,
                AccountType.SAVINGS,
                BigDecimal.ZERO
        );
        bankAccountRepository.save(account);

        String token = jwtService.generateToken(user.getUsername(), user.getRole().name());
        return new LoginResponse(token, user.getUsername(), user.getRole().name(),
                customer.getCustomerId(),
                "Registration successful. Your account number is " + account.getAccountNumber());
    }
}
