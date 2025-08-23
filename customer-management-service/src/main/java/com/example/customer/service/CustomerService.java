package com.example.customer.service;

import com.example.customer.domain.Address;
import com.example.customer.domain.Customer;
import com.example.customer.dto.*;
import com.example.customer.repository.AddressRepository;
import com.example.customer.repository.CustomerRepository;
import com.example.customer.util.JwtUtil;
import com.example.customer.util.PasswordUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static java.lang.Long.valueOf;

@Service
public class CustomerService {

    private final CustomerRepository customers;
    private final AddressRepository addresses;
    private final PasswordUtil passwordUtil;
    private final JwtUtil jwtUtil;

    public CustomerService(CustomerRepository customers,
                           AddressRepository addresses,
                           PasswordUtil passwordUtil,
                           JwtUtil jwtUtil) {
        this.customers = customers;
        this.addresses = addresses;
        this.passwordUtil = passwordUtil;
        this.jwtUtil = jwtUtil;
    }

    @Transactional
    public CustomerResponse signup(SignupRequest req) {
        String email = req.email().toLowerCase();
        if (customers.existsByEmailIgnoreCase(email)) {
            throw new IllegalArgumentException("Email already registered");
        }
        Customer c = new Customer();
        c.setEmail(email);
        c.setPasswordHash(passwordUtil.hash(req.password()));
        c.setFullName(req.fullName());
        c.setPhone(req.phone());
        Customer saved = customers.save(c);
        return new CustomerResponse(valueOf(saved.getId()), (saved.getEmail()), saved.getFullName(), saved.getPhone());
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest req) {
        Customer c = customers.findByEmailIgnoreCase(req.email())
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));
        if (!passwordUtil.matches(req.password(), c.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid email or password");
        }

        // ✅ Generate JWT with both email & customerId
        String token = jwtUtil.generateToken(c.getId(), c.getEmail());
        return new LoginResponse(c.getId(), c.getEmail(), token);
    }

    @Transactional(readOnly = true)
    public Customer authenticate(String token) {
        if (!jwtUtil.validateToken(token)) {
            throw new IllegalArgumentException("Invalid or expired token");
        }
        Long customerId = jwtUtil.extractCustomerId(token);
        if (customerId == null) throw new IllegalArgumentException("Invalid token - missing customer id");
        return customers.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found"));
    }

    @Transactional
    public AddressResponse addAddress(Long customerId, AddressRequest req) {
        Customer c = customers.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found"));

        Address a = new Address();
        a.setCustomer(c);
        a.setLine1(req.line1());
        a.setLine2(req.line2());
        a.setCity(req.city());
        a.setState(req.state());
        a.setPostalCode(req.postalCode());
        a.setCountry(req.country());

        boolean makeDefault = Boolean.TRUE.equals(req.makeDefault());
        boolean firstAddress = addresses.countByCustomer(c) == 0;

        a.setDefault(makeDefault || firstAddress);
        Address saved = addresses.save(a);

        if (saved.isDefault()) {
            unsetOtherDefaults(c.getId(), saved.getId());
        }

        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<AddressResponse> listAddresses(Long customerId) {
        Customer c = customers.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found"));
        return addresses.findByCustomer(c).stream().map(this::toResponse).toList();
    }

    @Transactional
    public void setDefaultAddress(Long customerId, Long addressId) {
        Customer c = customers.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found"));
        Address addr = addresses.findByIdAndCustomer(addressId, c)
                .orElseThrow(() -> new IllegalArgumentException("Address not found"));
        if (!addr.isDefault()) {
            addr.setDefault(true);
            addresses.save(addr);
            unsetOtherDefaults(customerId, addr.getId());
        }
    }

    private void unsetOtherDefaults(Long customerId, Long keepAddressId) {
        Customer c = customers.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found"));
        List<Address> all = addresses.findByCustomer(c);
        for (Address other : all) {
            if (!other.getId().equals(keepAddressId) && other.isDefault()) {
                other.setDefault(false);
                addresses.save(other);
            }
        }
    }

    private AddressResponse toResponse(Address a) {
        return new AddressResponse(
                a.getId(), a.getLine1(), a.getLine2(), a.getCity(), a.getState(),
                a.getPostalCode(), a.getCountry(), a.isDefault()
        );
    }
}
