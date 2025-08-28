
package com.example.customer.service;

import com.example.customer.domain.Address;
import com.example.customer.domain.Customer;
import com.example.customer.dto.AddressRequest;
import com.example.customer.dto.SignupRequest;
import com.example.customer.dto.LoginResponse;
import com.example.customer.dto.CustomerResponse;
import com.example.customer.dto.AddressResponse;
import com.example.customer.dto.LoginRequest;
import com.example.customer.repository.AddressRepository;
import com.example.customer.repository.CustomerRepository;
import com.example.customer.util.JwtUtil;
import com.example.customer.util.PasswordUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.function.Executable;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CustomerServiceTest {

    @InjectMocks
    private CustomerService service;

    @Mock
    private CustomerRepository customerRepo;

    @Mock
    private AddressRepository addressRepo;

    @Mock
    private PasswordUtil passwordUtil;

    @Mock
    private JwtUtil jwtUtil;

    @BeforeEach
    void init() {
        MockitoAnnotations.openMocks(this);
    }

    // ==================== Signup ====================
    @Test
    void testSignupSuccess() {
        final SignupRequest req = new SignupRequest("Test@Example.com", "password123", "John Doe", "1234567890");

        when(customerRepo.existsByEmailIgnoreCase("test@example.com")).thenReturn(false);
        when(passwordUtil.hash("password123")).thenReturn("hashedPwd");

        Customer saved = new Customer();
        saved.setId(1L);
        saved.setEmail("test@example.com");
        saved.setFullName("John Doe");
        saved.setPhone("1234567890");
        when(customerRepo.save(any(Customer.class))).thenReturn(saved);

        CustomerResponse resp = service.signup(req);

        assertEquals(1L, resp.id());
        assertEquals("test@example.com", resp.email()); // email stored lowercased
        assertEquals("John Doe", resp.fullName());
        assertEquals("1234567890", resp.phone());
    }

    @Test
    void testSignupDuplicateEmail() {
        final SignupRequest req = new SignupRequest("test@example.com", "pwd", "John", "1234");
        when(customerRepo.existsByEmailIgnoreCase("test@example.com")).thenReturn(true);

        Exception ex = assertThrows(IllegalArgumentException.class, new Executable() {
            @Override
            public void execute() throws Throwable {
                service.signup(req);
            }
        });
        assertEquals("Email already registered", ex.getMessage());
    }

    // ==================== Login ====================
    @Test
    void testLoginSuccess() {
        final LoginRequest req = new LoginRequest("test@example.com", "pwd");

        Customer c = new Customer();
        c.setId(1L);
        c.setEmail("test@example.com");
        c.setPasswordHash("hashedPwd");

        when(customerRepo.findByEmailIgnoreCase("test@example.com")).thenReturn(Optional.of(c));
        when(passwordUtil.matches("pwd", "hashedPwd")).thenReturn(true);
        when(jwtUtil.generateToken(1L, "test@example.com")).thenReturn("token123");

        LoginResponse resp = service.login(req);

        assertEquals(1L, resp.customerId());
        assertEquals("test@example.com", resp.email());
        assertEquals("token123", resp.token());
    }

    @Test
    void testLoginInvalidEmail() {
        when(customerRepo.findByEmailIgnoreCase("test@example.com")).thenReturn(Optional.empty());

        Exception ex = assertThrows(IllegalArgumentException.class, new Executable() {
            @Override
            public void execute() throws Throwable {
                service.login(new LoginRequest("test@example.com", "pwd"));
            }
        });
        assertEquals("Invalid email or password", ex.getMessage());
    }

    @Test
    void testLoginInvalidPassword() {
        Customer c = new Customer();
        c.setPasswordHash("hashedPwd");
        when(customerRepo.findByEmailIgnoreCase("test@example.com")).thenReturn(Optional.of(c));
        when(passwordUtil.matches("pwd", "hashedPwd")).thenReturn(false);

        Exception ex = assertThrows(IllegalArgumentException.class, new Executable() {
            @Override
            public void execute() throws Throwable {
                service.login(new LoginRequest("test@example.com", "pwd"));
            }
        });
        assertEquals("Invalid email or password", ex.getMessage());
    }

    // ==================== Authenticate ====================
    @Test
    void testAuthenticateSuccess() {
        final String token = "token123";

        when(jwtUtil.validateToken(token)).thenReturn(true);
        when(jwtUtil.extractCustomerId(token)).thenReturn(1L);

        Customer c = new Customer();
        when(customerRepo.findById(1L)).thenReturn(Optional.of(c));

        Customer result = service.authenticate(token);
        assertEquals(c, result);
    }

    @Test
    void testAuthenticateInvalidToken() {
        when(jwtUtil.validateToken("token123")).thenReturn(false);

        Exception ex = assertThrows(IllegalArgumentException.class, new Executable() {
            @Override
            public void execute() throws Throwable {
                service.authenticate("token123");
            }
        });
        assertEquals("Invalid or expired token", ex.getMessage());
    }

    @Test
    void testAuthenticateMissingCustomerId() {
        when(jwtUtil.validateToken("token123")).thenReturn(true);
        when(jwtUtil.extractCustomerId("token123")).thenReturn(null);

        Exception ex = assertThrows(IllegalArgumentException.class, new Executable() {
            @Override
            public void execute() throws Throwable {
                service.authenticate("token123");
            }
        });
        assertEquals("Invalid token - missing customer id", ex.getMessage());
    }

    @Test
    void testAuthenticateCustomerNotFound() {
        when(jwtUtil.validateToken("token123")).thenReturn(true);
        when(jwtUtil.extractCustomerId("token123")).thenReturn(1L);
        when(customerRepo.findById(1L)).thenReturn(Optional.empty());

        Exception ex = assertThrows(IllegalArgumentException.class, new Executable() {
            @Override
            public void execute() throws Throwable {
                service.authenticate("token123");
            }
        });
        assertEquals("Customer not found", ex.getMessage());
    }

    // ==================== Add Address ====================
    // ==================== Add Address ====================
    @Test
    void testAddAddressFirstDefault() {
        final Long customerId = 1L;
        final Customer c = new Customer();
        c.setId(customerId);

        when(customerRepo.findById(customerId)).thenReturn(Optional.of(c));
        when(addressRepo.findByCustomer(c)).thenReturn(new ArrayList<Address>());

        AddressRequest req = new AddressRequest("Line1", "Line2" ,"City", "State", "12345", "Country",false);
        final Address saved = new Address();
        saved.setId(1L);
        saved.setCustomer(c);
        saved.setLine1("Line1");
        saved.setDefault(true);

        when(addressRepo.save(any(Address.class))).thenReturn(saved);

        AddressResponse resp = service.addAddress(customerId, req);

        assertTrue(resp.isDefault());
        assertEquals("Line1", resp.line1());
    }

    @Test
    void testAddAddressNonExistentCustomer() {
        when(customerRepo.findById(1L)).thenReturn(Optional.empty());

        Exception ex = assertThrows(IllegalArgumentException.class, new Executable() {
            @Override
            public void execute() throws Throwable {
                service.addAddress(1L, new AddressRequest("L1", "Line2","C", "S", "P", "CN", false));
            }
        });
        assertEquals("Customer not found", ex.getMessage());
    }

    // ==================== List Addresses ====================
    @Test
    void testListAddressesEmpty() {
        final Long customerId = 1L;
        final Customer c = new Customer();
        when(customerRepo.findById(customerId)).thenReturn(Optional.of(c));
        when(addressRepo.findByCustomer(c)).thenReturn(new ArrayList<Address>());

        List<AddressResponse> result = service.listAddresses(customerId);
        assertTrue(result.isEmpty());
    }

    @Test
    void testListAddressesMultiple() {
        final Long customerId = 1L;
        final Customer c = new Customer();
        when(customerRepo.findById(customerId)).thenReturn(Optional.of(c));

        final Address a1 = new Address();
        a1.setId(1L);
        a1.setLine1("L1");
        a1.setDefault(true);

        final Address a2 = new Address();
        a2.setId(2L);
        a2.setLine1("L2");
        a2.setDefault(false);

        when(addressRepo.findByCustomer(c)).thenReturn(Arrays.asList(a1, a2));

        List<AddressResponse> result = service.listAddresses(customerId);
        assertEquals(2, result.size());
        assertTrue(result.get(0).isDefault());
    }

    // ==================== Set Default Address ====================
    @Test
    void testSetDefaultAddress() {
        final Long customerId = 1L;
        final Long addressId = 2L;
        final Customer c = new Customer();
        when(customerRepo.findById(customerId)).thenReturn(Optional.of(c));

        final Address a1 = new Address();
        a1.setId(1L);
        a1.setDefault(true);

        final Address a2 = new Address();
        a2.setId(addressId);
        a2.setDefault(false);

        when(addressRepo.findByIdAndCustomer(addressId, c)).thenReturn(Optional.of(a2));
        when(addressRepo.findByCustomer(c)).thenReturn(Arrays.asList(a1, a2));

        service.setDefaultAddress(customerId, addressId);

        assertTrue(a2.isDefault());
        assertFalse(a1.isDefault());
    }
}