package com.example.customer.controller;

import com.example.customer.dto.CustomerResponse;
import com.example.customer.service.CustomerService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    private final CustomerService svc;

    public CustomerController(CustomerService svc) {
        this.svc = svc;
    }

    /**
     * Return authenticated customer's profile.
     */
    @GetMapping("/me")
    public ResponseEntity<CustomerResponse> getProfile(HttpServletRequest request) {
        Long customerId = (Long) request.getAttribute("authenticatedCustomerId");
        CustomerResponse res = svc.getCustomerById(customerId);
        return ResponseEntity.ok(res);
    }

    @PutMapping("/email")
    public ResponseEntity<CustomerResponse> updateEmail(@RequestBody Map<String, String> body,
                                                        HttpServletRequest request) {
        Long customerId = (Long) request.getAttribute("authenticatedCustomerId");
        String newEmail = body.get("email");
        CustomerResponse updated = svc.updateEmail(customerId, newEmail);
        return ResponseEntity.ok(updated);
    }

    @PutMapping("/phone")
    public ResponseEntity<CustomerResponse> updatePhone(@RequestBody Map<String, String> body,
                                                        HttpServletRequest request) {
        Long customerId = (Long) request.getAttribute("authenticatedCustomerId");
        String newPhone = body.get("phone");
        CustomerResponse updated = svc.updatePhone(customerId, newPhone);
        return ResponseEntity.ok(updated);
    }

    /**
     * Suspend (disable) the authenticated customer's account.
     * This is safer than delete — data is preserved and account can be reactivated.
     */
    @PatchMapping("/suspend")
    public ResponseEntity<CustomerResponse> suspendAccount(HttpServletRequest request) {
        Long customerId = (Long) request.getAttribute("authenticatedCustomerId");
        CustomerResponse updated = svc.suspendCustomer(customerId);
        return ResponseEntity.ok(updated);
    }

    /**
     * Reactivate a previously suspended account.
     */
    @PatchMapping("/reactivate")
    public ResponseEntity<CustomerResponse> reactivateAccount(HttpServletRequest request) {
        Long customerId = (Long) request.getAttribute("authenticatedCustomerId");
        CustomerResponse updated = svc.reactivateCustomer(customerId);
        return ResponseEntity.ok(updated);
    }
}
