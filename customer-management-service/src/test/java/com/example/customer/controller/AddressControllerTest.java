package com.example.customer.controller;

import com.example.customer.dto.AddressRequest;
import com.example.customer.dto.AddressResponse;
import com.example.customer.service.CustomerService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;



@WebMvcTest(AddressController.class)
@AutoConfigureMockMvc(addFilters = false) // ✅ disable Spring Security
class AddressControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CustomerService svc;

    @Autowired
    private ObjectMapper objectMapper;

    private static final Long CUSTOMER_ID = 1L;

    @Test
    void testAddAddress() throws Exception {
        AddressRequest req = new AddressRequest("123 St", "Apt 1", "City", "State", "12345", "Country", true);
        AddressResponse res = new AddressResponse(10L, "123 St", "Apt 1", "City", "State", "12345", "Country", true);

        Mockito.when(svc.addAddress(eq(CUSTOMER_ID), any(AddressRequest.class))).thenReturn(res);

        mockMvc.perform(post("/api/customers/addresses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req))
                        .requestAttr("authenticatedCustomerId", CUSTOMER_ID))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/customers/addresses/10"))
                .andExpect(jsonPath("$.id").value(10L))
                .andExpect(jsonPath("$.line1").value("123 St"));
    }

    @Test
    void testListAddresses() throws Exception {
        List<AddressResponse> list = List.of(
                new AddressResponse(1L, "123 St", "Apt 1", "City", "State", "12345", "Country", true),
                new AddressResponse(2L, "456 Rd", null, "Town", "ST", "54321", "Country", false)
        );

        Mockito.when(svc.listAddresses(CUSTOMER_ID)).thenReturn(list);

        mockMvc.perform(get("/api/customers/addresses")
                        .requestAttr("authenticatedCustomerId", CUSTOMER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[1].line1").value("456 Rd"));
    }

    @Test
    void testSetDefaultAddress() throws Exception {
        Mockito.doNothing().when(svc).setDefaultAddress(CUSTOMER_ID, 5L);

        mockMvc.perform(put("/api/customers/addresses/5/default")
                        .requestAttr("authenticatedCustomerId", CUSTOMER_ID))
                .andExpect(status().isNoContent());
    }

    @Test
    void testUpdateAddress() throws Exception {
        AddressRequest req = new AddressRequest("New St", null, "NewCity", null, "99999", "NewCountry", false);
        AddressResponse updated = new AddressResponse(5L, "New St", null, "NewCity", null, "99999", "NewCountry", false);

        Mockito.when(svc.updateAddress(eq(CUSTOMER_ID), eq(5L), any(AddressRequest.class))).thenReturn(updated);

        mockMvc.perform(put("/api/customers/addresses/5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req))
                        .requestAttr("authenticatedCustomerId", CUSTOMER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5L))
                .andExpect(jsonPath("$.line1").value("New St"))
                .andExpect(jsonPath("$.postalCode").value("99999"));
    }

    @Test
    void testPatchAddress() throws Exception {
        Map<String, Object> updates = Map.of("city", "PatchedCity");
        AddressResponse patched = new AddressResponse(5L, "New St", null, "PatchedCity", null, "99999", "NewCountry", false);

        Mockito.when(svc.patchAddress(eq(CUSTOMER_ID), eq(5L), anyMap())).thenReturn(patched);

        mockMvc.perform(patch("/api/customers/addresses/5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updates))
                        .requestAttr("authenticatedCustomerId", CUSTOMER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.city").value("PatchedCity"));
    }

    @Test
    void testDeleteAddress() throws Exception {
        Mockito.doNothing().when(svc).deleteAddress(CUSTOMER_ID, 5L);

        mockMvc.perform(delete("/api/customers/addresses/5")
                        .requestAttr("authenticatedCustomerId", CUSTOMER_ID))
                .andExpect(status().isNoContent());
    }
}