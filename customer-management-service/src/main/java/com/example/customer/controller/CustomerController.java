package com.example.customer.controller;

import com.example.customer.dto.*;
import com.example.customer.service.CustomerService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    private final CustomerService svc;

    public CustomerController(CustomerService svc) {
        this.svc = svc;
    }

    @PostMapping("/signup")
    public ResponseEntity<CustomerResponse> signup(@RequestBody @Valid SignupRequest req) {
        CustomerResponse res = svc.signup(req);
        return ResponseEntity.created(URI.create("/api/customers/" + res.id())).body(res);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody @Valid LoginRequest req) {
        return ResponseEntity.ok(svc.login(req));
    }
    @PutMapping("/email")
    public ResponseEntity<CustomerResponse> updateEmail(@RequestBody Map<String, String> body,
                                                        HttpServletRequest request) {
        Long customerId = (Long) request.getAttribute("authenticatedCustomerId");
        String newEmail = body.get("email");
        CustomerResponse updated = svc.updateEmail(customerId, newEmail);
        return ResponseEntity.ok(updated);
    }

    @PutMapping("/password")
    public ResponseEntity<Void> updatePassword(@RequestBody Map<String, String> body,
                                               HttpServletRequest request) {
        Long customerId = (Long) request.getAttribute("authenticatedCustomerId");
        String oldPassword = body.get("oldPassword");
        String newPassword = body.get("newPassword");
        svc.updatePassword(customerId, oldPassword, newPassword);
        return ResponseEntity.noContent().build(); // return 204 on success
    }

    @PutMapping("/phone")
    public ResponseEntity<CustomerResponse> updatePhone(@RequestBody Map<String, String> body,
                                                        HttpServletRequest request) {
        Long customerId = (Long) request.getAttribute("authenticatedCustomerId");
        String newPhone = body.get("phone");
        CustomerResponse updated = svc.updatePhone(customerId, newPhone);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteCustomer(HttpServletRequest request) {
        Long customerId = (Long) request.getAttribute("authenticatedCustomerId");
        svc.deleteCustomer(customerId);
        return ResponseEntity.noContent().build(); // 204
    }

    @PostMapping("/addresses")
    public ResponseEntity<AddressResponse> addAddress(@RequestBody @Valid AddressRequest req,
                                                      HttpServletRequest request) {
        Long customerId = (Long) request.getAttribute("authenticatedCustomerId");
//        logger.debug("Authenticated customerId: {}", customerId);
        AddressResponse res = svc.addAddress(customerId, req);
        return ResponseEntity.created(URI.create("/api/customers/addresses/" + res.id()))
                .body(res);
    }

    @GetMapping("/addresses")
    public List<AddressResponse> list(HttpServletRequest request) {
        Long customerId = (Long) request.getAttribute("authenticatedCustomerId");
        return svc.listAddresses(customerId);
    }

    @PutMapping("/addresses/{addressId}/default")
    public ResponseEntity<Void> setDefault(@PathVariable("addressId") Long addressId,
                                           HttpServletRequest request) {
        Long customerId = (Long) request.getAttribute("authenticatedCustomerId");
        svc.setDefaultAddress(customerId, addressId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/addresses/{addressId}")
    public ResponseEntity<AddressResponse> updateAddress(@PathVariable("addressId") Long addressId,
                                                         @RequestBody AddressRequest req,
                                                         HttpServletRequest request) {
        Long customerId = (Long) request.getAttribute("authenticatedCustomerId");
        AddressResponse updated = svc.updateAddress(customerId, addressId, req);
        return ResponseEntity.ok(updated); // 200 OK with updated object
    }

    @PatchMapping("/addresses/{addressId}")
    public ResponseEntity<AddressResponse> patchAddress(@PathVariable("addressId") Long addressId,
                                                        @RequestBody Map<String, Object> updates,
                                                        HttpServletRequest request) {
        Long customerId = (Long) request.getAttribute("authenticatedCustomerId");
        AddressResponse updated = svc.patchAddress(customerId, addressId, updates);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/addresses/{addressId}")
    public ResponseEntity<Void> deleteAddress(@PathVariable("addressId") Long addressId,
                                              HttpServletRequest request) {
        Long customerId = (Long) request.getAttribute("authenticatedCustomerId");
        svc.deleteAddress(customerId, addressId);
        return ResponseEntity.noContent().build(); // 204 No Content
    }
}
