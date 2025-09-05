package com.example.customer.controller;

import com.example.customer.dto.CustomerResponse;
import com.example.customer.service.CustomerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class CustomerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CustomerService svc;

    private CustomerResponse customer;

    @BeforeEach
    void setup() {
        customer = new CustomerResponse(1L, "john@example.com", "John Doe", "9876543210");
    }

    @Test
    void testGetProfile() throws Exception {
        Mockito.when(svc.getCustomerById(1L)).thenReturn(customer);

        mockMvc.perform(get("/api/customers/me")
                        .requestAttr("authenticatedCustomerId", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.email").value("john@example.com"));
    }

    @Test
    void testUpdateEmail() throws Exception {
        CustomerResponse updated = new CustomerResponse(1L, "newjohn@example.com", "John Doe", "9876543210");
        Mockito.when(svc.updateEmail(eq(1L), eq("newjohn@example.com"))).thenReturn(updated);

        mockMvc.perform(put("/api/customers/email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"newjohn@example.com\"}")
                        .requestAttr("authenticatedCustomerId", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("newjohn@example.com"));
    }

    @Test
    void testUpdatePhone() throws Exception {
        CustomerResponse updated = new CustomerResponse(1L, "john@example.com", "John Doe", "9998887777");
        Mockito.when(svc.updatePhone(eq(1L), eq("9998887777"))).thenReturn(updated);

        mockMvc.perform(put("/api/customers/phone")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"phone\":\"9998887777\"}")
                        .requestAttr("authenticatedCustomerId", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.phone").value("9998887777"));
    }

    @Test
    void testSuspendAccount() throws Exception {
        CustomerResponse suspended = new CustomerResponse(1L, "john@example.com", "John Doe", "9876543210");
        Mockito.when(svc.suspendCustomer(1L)).thenReturn(suspended);

        mockMvc.perform(patch("/api/customers/suspend")
                        .requestAttr("authenticatedCustomerId", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("john@example.com"));
    }

    @Test
    void testReactivateAccount() throws Exception {
        CustomerResponse reactivated = new CustomerResponse(1L, "john@example.com", "John Doe", "9876543210");
        Mockito.when(svc.reactivateCustomer(1L)).thenReturn(reactivated);

        mockMvc.perform(patch("/api/customers/reactivate")
                        .requestAttr("authenticatedCustomerId", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("john@example.com"));
    }
}
