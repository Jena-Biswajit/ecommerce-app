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
import java.util.Map;

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
    public CustomerResponse updateEmail(Long customerId, String newEmail) {
        Customer c = customers.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found"));

        if (customers.existsByEmailIgnoreCase(newEmail)) {
            throw new IllegalArgumentException("Email already registered");
        }

        c.setEmail(newEmail.toLowerCase());
        Customer saved = customers.save(c);
        return new CustomerResponse(saved.getId(), saved.getEmail(), saved.getFullName(), saved.getPhone());
    }

    @Transactional
    public void updatePassword(Long customerId, String oldPassword, String newPassword) {
        Customer c = customers.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found"));

        if (!passwordUtil.matches(oldPassword, c.getPasswordHash())) {
            throw new IllegalArgumentException("Old password is incorrect");
        }

        c.setPasswordHash(passwordUtil.hash(newPassword));
        customers.save(c);
    }

    @Transactional
    public CustomerResponse updatePhone(Long customerId, String newPhone) {
        Customer c = customers.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found"));

        c.setPhone(newPhone);
        Customer saved = customers.save(c);
        return new CustomerResponse(saved.getId(), saved.getEmail(), saved.getFullName(), saved.getPhone());
    }

    @Transactional
    public void deleteCustomer(Long customerId) {
        Customer c = customers.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found"));

        // delete all addresses first
        addresses.deleteAll(addresses.findByCustomer(c));

        // delete customer account
        customers.delete(c);
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

    @Transactional
    public AddressResponse updateAddress(Long customerId, Long addressId, AddressRequest req) {
        Customer c = customers.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found"));

        Address addr = addresses.findByIdAndCustomer(addressId, c)
                .orElseThrow(() -> new IllegalArgumentException("Address not found"));

        // update fields
        addr.setLine1(req.line1());
        addr.setLine2(req.line2());
        addr.setCity(req.city());
        addr.setState(req.state());
        addr.setPostalCode(req.postalCode());
        addr.setCountry(req.country());

        Address saved = addresses.save(addr);
        return toResponse(saved);
    }

    @Transactional
    public AddressResponse patchAddress(Long customerId, Long addressId, Map<String, Object> updates) {
        Customer c = customers.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found"));

        Address addr = addresses.findByIdAndCustomer(addressId, c)
                .orElseThrow(() -> new IllegalArgumentException("Address not found"));

        // Only update fields present in the map
        if (updates.containsKey("line1")) addr.setLine1((String) updates.get("line1"));
        if (updates.containsKey("line2")) addr.setLine2((String) updates.get("line2"));
        if (updates.containsKey("city")) addr.setCity((String) updates.get("city"));
        if (updates.containsKey("state")) addr.setState((String) updates.get("state"));
        if (updates.containsKey("postalCode")) addr.setPostalCode((String) updates.get("postalCode"));
        if (updates.containsKey("country")) addr.setCountry((String) updates.get("country"));
        if (updates.containsKey("default")) addr.setDefault((Boolean) updates.get("default"));

        return toResponse(addresses.save(addr));
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

    @Transactional
    public void deleteAddress(Long customerId, Long addressId) {
        Customer c = customers.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found"));

        Address addr = addresses.findByIdAndCustomer(addressId, c)
                .orElseThrow(() -> new IllegalArgumentException("Address not found"));

        addresses.delete(addr);
    }

    private AddressResponse toResponse(Address a) {
        return new AddressResponse(
                a.getId(), a.getLine1(), a.getLine2(), a.getCity(), a.getState(),
                a.getPostalCode(), a.getCountry(), a.isDefault()
        );
    }
}
