package com.example.bankmanagement.service;

import com.example.bankmanagement.dto.CustomerResponse;
import com.example.bankmanagement.dto.UpdateProfileRequest;
import com.example.bankmanagement.entity.Customer;
import com.example.bankmanagement.entity.User;
import com.example.bankmanagement.exception.BadRequestException;
import com.example.bankmanagement.exception.ResourceNotFoundException;
import com.example.bankmanagement.repository.CustomerRepository;
import com.example.bankmanagement.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CustomerService {

    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;

    public CustomerService(UserRepository userRepository, CustomerRepository customerRepository) {
        this.userRepository = userRepository;
        this.customerRepository = customerRepository;
    }

    public Customer getCustomerByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        if (user.getCustomer() == null) {
            throw new ResourceNotFoundException("Customer profile not found for this user");
        }
        return user.getCustomer();
    }

    public CustomerResponse getProfile(String username) {
        return new CustomerResponse(getCustomerByUsername(username));
    }

    @Transactional
    public CustomerResponse updateProfile(String username, UpdateProfileRequest request) {
        Customer customer = getCustomerByUsername(username);

        // Email must stay unique (excluding this customer)
        customerRepository.findByEmail(request.getEmail())
                .filter(other -> !other.getId().equals(customer.getId()))
                .ifPresent(other -> {
                    throw new BadRequestException("Email is already in use");
                });

        customer.setFirstName(request.getFirstName());
        customer.setLastName(request.getLastName());
        customer.setEmail(request.getEmail());
        customer.setPhone(request.getPhone());
        customer.setAddress(request.getAddress());
        customer.setDateOfBirth(request.getDateOfBirth());
        customerRepository.save(customer);

        return new CustomerResponse(customer);
    }
}
