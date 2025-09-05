package com.example.customer.controller;

import com.example.customer.dto.AddressRequest;
import com.example.customer.dto.AddressResponse;
import com.example.customer.service.CustomerService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;


import java.net.URI;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/customers/addresses")
public class AddressController {

    private final CustomerService svc;

    public AddressController(CustomerService svc) {
        this.svc = svc;
    }

    @PostMapping
    public ResponseEntity<AddressResponse> addAddress(@RequestBody @Valid AddressRequest req,
                                                      HttpServletRequest request) {
        Long customerId = (Long) request.getAttribute("authenticatedCustomerId");
        System.out.println(customerId);
        AddressResponse res = svc.addAddress(customerId, req);
        return ResponseEntity.created(URI.create("/api/customers/addresses/" + res.id()))
                .body(res);
    }

    @GetMapping
    public ResponseEntity<List<AddressResponse>> list(HttpServletRequest request) {
        Long customerId = (Long) request.getAttribute("authenticatedCustomerId");
        List<AddressResponse> addresses = svc.listAddresses(customerId);
        return ResponseEntity.ok(addresses);
    }

    @PutMapping("/{addressId}/default")
    public ResponseEntity<Void> setDefault(@PathVariable("addressId") Long addressId,
                                           HttpServletRequest request) {
        Long customerId = (Long) request.getAttribute("authenticatedCustomerId");
        svc.setDefaultAddress(customerId, addressId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{addressId}")
    public ResponseEntity<AddressResponse> updateAddress(@PathVariable("addressId") Long addressId,
                                                         @RequestBody AddressRequest req,
                                                         HttpServletRequest request) {
        Long customerId = (Long) request.getAttribute("authenticatedCustomerId");
        AddressResponse updated = svc.updateAddress(customerId, addressId, req);
        return ResponseEntity.ok(updated);
    }

    @PatchMapping("/{addressId}")
    public ResponseEntity<AddressResponse> patchAddress(@PathVariable("addressId") Long addressId,
                                                        @RequestBody Map<String, Object> updates,
                                                        HttpServletRequest request) {
        Long customerId = (Long) request.getAttribute("authenticatedCustomerId");
        AddressResponse updated = svc.patchAddress(customerId, addressId, updates);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{addressId}")
    public ResponseEntity<Void> deleteAddress(@PathVariable("addressId") Long addressId,
                                              HttpServletRequest request) {
        Long customerId = (Long) request.getAttribute("authenticatedCustomerId");
        svc.deleteAddress(customerId, addressId);
        return ResponseEntity.noContent().build();
    }
}
