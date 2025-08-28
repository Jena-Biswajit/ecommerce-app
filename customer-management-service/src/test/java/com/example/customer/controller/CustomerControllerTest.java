
package com.example.customer.controller;
import com.example.customer.dto.AddressRequest;
import com.example.customer.dto.SignupRequest;
import com.example.customer.dto.LoginResponse;
import com.example.customer.dto.CustomerResponse;
import com.example.customer.dto.AddressResponse;
import com.example.customer.dto.LoginRequest;
import com.example.customer.service.CustomerService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
    }

    @Test
    public void testUpdateEmail() throws Exception {
        Map<String, String> body = Map.of("email", "new@example.com");
        CustomerResponse res = new CustomerResponse(1L, "new@example.com", "John Doe", "1234567890");

        when(customerService.updateEmail(1L, "new@example.com")).thenReturn(res);

        mockMvc.perform(put("/api/customers/email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body))
                        .requestAttr("authenticatedCustomerId", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("new@example.com"));
    }

    @Test
    public void testUpdatePassword() throws Exception {
        Map<String, String> body = Map.of("oldPassword", "old123", "newPassword", "new123");

        doNothing().when(customerService).updatePassword(1L, "old123", "new123");

        mockMvc.perform(put("/api/customers/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body))
                        .requestAttr("authenticatedCustomerId", 1L))
                .andExpect(status().isNoContent());
    }

    @Test
    public void testUpdatePhone() throws Exception {
        Map<String, String> body = Map.of("phone", "9876543210");
        CustomerResponse res = new CustomerResponse(1L, "test@example.com", "John Doe", "9876543210");

        when(customerService.updatePhone(1L, "9876543210")).thenReturn(res);

        mockMvc.perform(put("/api/customers/phone")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body))
                        .requestAttr("authenticatedCustomerId", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.phone").value("9876543210"));
    }

    @Test
    public void testDeleteCustomer() throws Exception {
        doNothing().when(customerService).deleteCustomer(1L);

        mockMvc.perform(delete("/api/customers")
                        .requestAttr("authenticatedCustomerId", 1L))
                .andExpect(status().isNoContent());
    }

    @Test
    public void testAddAddressSuccess() throws Exception {
        AddressRequest req = new AddressRequest("123 Street", "Apt 4", "CityX", "StateX", "12345", "CountryX", true);
        AddressResponse res = new AddressResponse(1L, req.line1(), req.line2(), req.city(), req.state(), req.postalCode(), req.country(), true);

        when(customerService.addAddress(eq(1L), any(AddressRequest.class))).thenReturn(res);

        mockMvc.perform(post("/api/customers/addresses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req))
                        .requestAttr("authenticatedCustomerId", 1L))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.line1").value("123 Street"));
    }

    @Test
    public void testListAddresses() throws Exception {
        List<AddressResponse> addresses = Arrays.asList(
                new AddressResponse(1L, "123 Street", "Apt 4", "CityX", "StateX", "12345", "CountryX", true),
                new AddressResponse(2L, "456 Avenue", "", "CityY", "StateY", "67890", "CountryY", false)
        );

        when(customerService.listAddresses(1L)).thenReturn(addresses);

        mockMvc.perform(get("/api/customers/addresses")
                        .requestAttr("authenticatedCustomerId", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[1].id").value(2L));
    }

    @Test
    public void testSetDefaultAddress() throws Exception {
        doNothing().when(customerService).setDefaultAddress(1L, 2L);

        mockMvc.perform(put("/api/customers/addresses/2/default")
                        .requestAttr("authenticatedCustomerId", 1L))
                .andExpect(status().isNoContent());
    }

    @Test
    public void testUpdateAddress() throws Exception {
        AddressRequest req = new AddressRequest("New Line", "L2", "CityN", "StateN", "99999", "CountryN", false);
        AddressResponse res = new AddressResponse(2L, "New Line", "L2", "CityN", "StateN", "99999", "CountryN", false);

        when(customerService.updateAddress(1L, 2L, req)).thenReturn(res);

        mockMvc.perform(put("/api/customers/addresses/2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req))
                        .requestAttr("authenticatedCustomerId", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.line1").value("New Line"));
    }

    @Test
    public void testPatchAddress() throws Exception {
        Map<String, Object> updates = Map.of("city", "PatchedCity");
        AddressResponse res = new AddressResponse(2L, "123 St", "L2", "PatchedCity", "State", "12345", "Country", false);

        when(customerService.patchAddress(1L, 2L, updates)).thenReturn(res);

        mockMvc.perform(patch("/api/customers/addresses/2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updates))
                        .requestAttr("authenticatedCustomerId", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.city").value("PatchedCity"));
    }

    @Test
    public void testDeleteAddress() throws Exception {
        doNothing().when(customerService).deleteAddress(1L, 2L);

        mockMvc.perform(delete("/api/customers/addresses/2")
                        .requestAttr("authenticatedCustomerId", 1L))
                .andExpect(status().isNoContent());
    }
}
