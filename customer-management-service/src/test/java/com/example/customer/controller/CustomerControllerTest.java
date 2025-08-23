package com.example.customer.controller;

import com.example.customer.dto.*;
import com.example.customer.service.CustomerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;

public class CustomerControllerTest {

    private MockMvc mockMvc;

    @Mock
    private CustomerService customerService;

    @InjectMocks
    private CustomerController customerController;

    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(customerController)
                .setControllerAdvice(new Object() {
                    @org.springframework.web.bind.annotation.ExceptionHandler(IllegalArgumentException.class)
                    public org.springframework.http.ResponseEntity<String> handleIllegalArg(IllegalArgumentException ex) {
                        return org.springframework.http.ResponseEntity.badRequest().body(ex.getMessage());
                    }
                })
                .build();
    }

    @Test
    public void testSignupSuccess() throws Exception {
        SignupRequest req = new SignupRequest("test@example.com", "password123", "John Doe", "1234567890");
        CustomerResponse res = new CustomerResponse(1L, req.email(), req.fullName(), req.phone());

        when(customerService.signup(any(SignupRequest.class))).thenReturn(res);

        mockMvc.perform(post("/api/customers/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.fullName").value("John Doe"))
                .andExpect(jsonPath("$.phone").value("1234567890"));

        verify(customerService, times(1)).signup(any(SignupRequest.class));
    }

    @Test
    public void testLoginSuccess() throws Exception {
        LoginRequest req = new LoginRequest("test@example.com", "password123");
        LoginResponse res = new LoginResponse(1L, req.email(), "dummy-token");

        when(customerService.login(any(LoginRequest.class))).thenReturn(res);

        mockMvc.perform(post("/api/customers/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customerId").value(1L))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.token").value("dummy-token"));

        verify(customerService, times(1)).login(any(LoginRequest.class));
    }

    @Test
    public void testAddAddressSuccess() throws Exception {
        AddressRequest req = new AddressRequest("123 Street", "Apt 4", "CityX", "StateX", "12345", "CountryX", true);
        AddressResponse res = new AddressResponse(1L, req.line1(), req.line2(), req.city(), req.state(), req.postalCode(), req.country(), true);

        when(customerService.addAddress(eq(1L), any(AddressRequest.class))).thenReturn(res);

        mockMvc.perform(post("/api/customers/1/addresses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req))
                        .requestAttr("authenticatedCustomerId", 1L))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.line1").value("123 Street"));

        verify(customerService, times(1)).addAddress(eq(1L), any(AddressRequest.class));
    }

    @Test
    public void testListAddresses() throws Exception {
        List<AddressResponse> addresses = Arrays.asList(
                new AddressResponse(1L, "123 Street", "Apt 4", "CityX", "StateX", "12345", "CountryX", true),
                new AddressResponse(2L, "456 Avenue", "", "CityY", "StateY", "67890", "CountryY", false)
        );

        when(customerService.listAddresses(1L)).thenReturn(addresses);

        mockMvc.perform(get("/api/customers/1/addresses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[1].id").value(2L));

        verify(customerService, times(1)).listAddresses(1L);
    }

    @Test
    public void testSetDefaultAddress() throws Exception {
        doNothing().when(customerService).setDefaultAddress(1L, 2L);

        mockMvc.perform(put("/api/customers/1/addresses/2/default"))
                .andExpect(status().isNoContent());

        verify(customerService, times(1)).setDefaultAddress(1L, 2L);
    }

    // Exception handling test
    @Test
    public void testSignupInvalidEmail() throws Exception {
        SignupRequest req = new SignupRequest("invalid-email", "password123", "John Doe", "1234567890");

        mockMvc.perform(post("/api/customers/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testForbiddenAddAddress() throws Exception {
        AddressRequest req = new AddressRequest("123 Street", "Apt 4", "CityX", "StateX", "12345", "CountryX", true);

        mockMvc.perform(post("/api/customers/1/addresses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req))
                        .requestAttr("authenticatedCustomerId", 99L)) // wrong auth ID
                .andExpect(status().isForbidden());
    }
}
