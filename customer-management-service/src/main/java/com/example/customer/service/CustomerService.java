package com.example.customer.service;

import com.example.customer.domain.Address;
import com.example.customer.domain.Customer;
import com.example.customer.dto.AddressRequest;
import com.example.customer.dto.AddressResponse;
import com.example.customer.dto.CustomerResponse;
import com.example.customer.repository.AddressRepository;
import com.example.customer.repository.CustomerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class CustomerService {

    private final CustomerRepository customers;
    private final AddressRepository addresses;

    public CustomerService(CustomerRepository customers,
                           AddressRepository addresses) {
        this.customers = customers;
        this.addresses = addresses;
    }

    // ================== CUSTOMER CRUD ==================

    @Transactional(readOnly = true)
    public CustomerResponse getCustomerById(Long customerId) {
        Customer c = customers.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found"));
        return new CustomerResponse(c.getId(), c.getEmail(), c.getFullName(), c.getPhone());
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
    public CustomerResponse updatePhone(Long customerId, String newPhone) {
        Customer c = customers.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found"));

        c.setPhone(newPhone);
        Customer saved = customers.save(c);
        return new CustomerResponse(saved.getId(), saved.getEmail(), saved.getFullName(), saved.getPhone());
    }

    /**
     * Suspend account instead of deleting
     */
    @Transactional
    public CustomerResponse suspendCustomer(Long customerId) {
        Customer c = customers.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found"));
        c.setStatus(Customer.Status.SUSPENDED);
        Customer saved = customers.save(c);
        return new CustomerResponse(saved.getId(), saved.getEmail(), saved.getFullName(), saved.getPhone());
    }

    /**
     * Reactivate a suspended account
     */
    @Transactional
    public CustomerResponse reactivateCustomer(Long customerId) {
        Customer c = customers.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found"));
        c.setStatus(Customer.Status.ACTIVE);
        Customer saved = customers.save(c);
        return new CustomerResponse(saved.getId(), saved.getEmail(), saved.getFullName(), saved.getPhone());
    }

    // ================== ADDRESS MANAGEMENT ==================

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

        addr.setLine1(req.line1());
        addr.setLine2(req.line2());
        addr.setCity(req.city());
        addr.setState(req.state());
        addr.setPostalCode(req.postalCode());
        addr.setCountry(req.country());

        return toResponse(addresses.save(addr));
    }

    @Transactional
    public AddressResponse patchAddress(Long customerId, Long addressId, Map<String, Object> updates) {
        Customer c = customers.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found"));

        Address addr = addresses.findByIdAndCustomer(addressId, c)
                .orElseThrow(() -> new IllegalArgumentException("Address not found"));

        if (updates.containsKey("line1")) addr.setLine1((String) updates.get("line1"));
        if (updates.containsKey("line2")) addr.setLine2((String) updates.get("line2"));
        if (updates.containsKey("city")) addr.setCity((String) updates.get("city"));
        if (updates.containsKey("state")) addr.setState((String) updates.get("state"));
        if (updates.containsKey("postalCode")) addr.setPostalCode((String) updates.get("postalCode"));
        if (updates.containsKey("country")) addr.setCountry((String) updates.get("country"));
        if (updates.containsKey("default")) addr.setDefault((Boolean) updates.get("default"));

        return toResponse(addresses.save(addr));
    }

    @Transactional
    public void deleteAddress(Long customerId, Long addressId) {
        Customer c = customers.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found"));

        Address addr = addresses.findByIdAndCustomer(addressId, c)
                .orElseThrow(() -> new IllegalArgumentException("Address not found"));

        addresses.delete(addr);
    }

    private void unsetOtherDefaults(Long customerId, Long keepAddressId) {
        Customer c = customers.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found"));

        // use the exact same reference
        List<Address> all = addresses.findByCustomer(c);
        for (Address other : all) {
            if (!Objects.equals(other.getId(), keepAddressId) && other.isDefault()) {
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
