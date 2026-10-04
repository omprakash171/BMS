package com.example.bankmanagement.repository;

import com.example.bankmanagement.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

    Optional<Customer> findByCustomerId(String customerId);

    Optional<Customer> findByEmail(String email);

    boolean existsByEmail(String email);

    List<Customer> findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCaseOrCustomerIdContainingIgnoreCaseOrEmailContainingIgnoreCase(
            String firstName, String lastName, String customerId, String email);
}
