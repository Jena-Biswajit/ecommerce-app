package com.example.customer.controller;

import com.example.customer.dto.*;
import com.example.customer.service.CustomerService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

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
    @PostMapping("/{customerId}/addresses")
    public ResponseEntity<AddressResponse> addAddress(@PathVariable("customerId") Long customerId,
                                                      @RequestBody @Valid AddressRequest req,
                                                      HttpServletRequest request) {
        // verify the authenticated user id matches the path param
        Long authCid = (Long) request.getAttribute("authenticatedCustomerId");
        if (authCid == null || !authCid.equals(customerId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        AddressResponse res = svc.addAddress(customerId, req);
        return ResponseEntity.created(URI.create("/api/customers/" + customerId + "/addresses/" + res.id()))
                .body(res);
    }


    @GetMapping("/{customerId}/addresses")
    public List<AddressResponse> list(@PathVariable("customerId") Long customerId) {
        return svc.listAddresses(customerId);
    }

    @PutMapping("/{customerId}/addresses/{addressId}/default")
    public ResponseEntity<Void> setDefault(
            @PathVariable("customerId") Long customerId,
            @PathVariable("addressId") Long addressId) {
        svc.setDefaultAddress(customerId, addressId);
        return ResponseEntity.noContent().build();
    }


    // Basic exception → 400 for validation/business errors
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArg(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(ex.getMessage());
    }
}
