//package com.example.customer.service;
//
//import com.example.customer.domain.Address;
//import com.example.customer.domain.Customer;
//import com.example.customer.dto.AddressRequest;
//import com.example.customer.dto.AddressResponse;
//import com.example.customer.dto.CustomerResponse;
//import com.example.customer.repository.AddressRepository;
//import com.example.customer.repository.CustomerRepository;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.mockito.Mockito;
//
//import java.util.*;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.Mockito.*;
//
//class CustomerServiceTest {
//
//    private CustomerRepository customers;
//    private AddressRepository addresses;
//    private CustomerService service;
//
//    private Customer customer;
//    private Address address;
//
//    @BeforeEach
//    void setup() {
//        customers = Mockito.mock(CustomerRepository.class);
//        addresses = Mockito.mock(AddressRepository.class);
//        service = new CustomerService(customers, addresses);
//
//        customer = new Customer();
//        customer.setId(1L);
//        customer.setEmail("test@example.com");
//        customer.setFullName("Test User");
//        customer.setPhone("9999999999");
//        customer.setStatus(Customer.Status.ACTIVE);
//
//        address = new Address();
//        address.setId(10L);
//        address.setCustomer(customer);
//        address.setLine1("Line1");
//        address.setCity("City");
//        address.setCountry("Country");
//        address.setDefault(false);
//    }
//
//    // ===== CUSTOMER TESTS =====
//
//    @Test
//    void testGetCustomerById_success() {
//        when(customers.findById(1L)).thenReturn(Optional.of(customer));
//
//        CustomerResponse resp = service.getCustomerById(1L);
//
//        assertEquals("test@example.com", resp.email());
//    }
//
//    @Test
//    void testGetCustomerById_notFound() {
//        when(customers.findById(2L)).thenReturn(Optional.empty());
//        assertThrows(IllegalArgumentException.class, () -> service.getCustomerById(2L));
//    }
//
//    @Test
//    void testUpdateEmail_success() {
//        when(customers.findById(1L)).thenReturn(Optional.of(customer));
//        when(customers.existsByEmailIgnoreCase("new@mail.com")).thenReturn(false);
//        when(customers.save(any(Customer.class))).thenReturn(customer);
//
//        CustomerResponse resp = service.updateEmail(1L, "new@mail.com");
//
//        assertEquals("new@mail.com", resp.email());
//    }
//
//    @Test
//    void testUpdateEmail_alreadyExists() {
//        when(customers.findById(1L)).thenReturn(Optional.of(customer));
//        when(customers.existsByEmailIgnoreCase("dup@mail.com")).thenReturn(true);
//
//        assertThrows(IllegalArgumentException.class, () -> service.updateEmail(1L, "dup@mail.com"));
//    }
//
//    @Test
//    void testUpdatePhone_success() {
//        when(customers.findById(1L)).thenReturn(Optional.of(customer));
//        when(customers.save(any(Customer.class))).thenReturn(customer);
//
//        CustomerResponse resp = service.updatePhone(1L, "12345");
//        assertEquals("12345", resp.phone());
//    }
//
//    @Test
//    void testSuspendCustomer_success() {
//        when(customers.findById(1L)).thenReturn(Optional.of(customer));
//        when(customers.save(any(Customer.class))).thenReturn(customer);
//
//        CustomerResponse resp = service.suspendCustomer(1L);
//        assertEquals(Customer.Status.SUSPENDED, customer.getStatus());
//    }
//
//    @Test
//    void testReactivateCustomer_success() {
//        customer.setStatus(Customer.Status.SUSPENDED);
//        when(customers.findById(1L)).thenReturn(Optional.of(customer));
//        when(customers.save(any(Customer.class))).thenReturn(customer);
//
//        CustomerResponse resp = service.reactivateCustomer(1L);
//        assertEquals(Customer.Status.ACTIVE, customer.getStatus());
//    }
//
//    // ===== ADDRESS TESTS =====
//
//    @Test
//    void testAddAddress_firstAddressBecomesDefault() {
//        AddressRequest req = new AddressRequest("L1", "L2", "City", "State", "12345", "India", false);
//
//        when(customers.findById(1L)).thenReturn(Optional.of(customer));
//        when(addresses.countByCustomer(customer)).thenReturn(0L);
//        when(addresses.save(any(Address.class))).thenReturn(address);
//
//        AddressResponse resp = service.addAddress(1L, req);
//
//        assertEquals("Line1", resp.line1()); // from address mock
//    }
//
//    @Test
//    void testAddAddress_makeDefaultTrue() {
//        AddressRequest req = new AddressRequest("L1", "L2", "City", "State", "12345", "India", true);
//
//        address.setDefault(true);
//
//        when(customers.findById(1L)).thenReturn(Optional.of(customer));
//        when(addresses.countByCustomer(customer)).thenReturn(1L);
//        when(addresses.save(any(Address.class))).thenReturn(address);
//        when(addresses.findByCustomer(customer)).thenReturn(Collections.singletonList(address));
//
//        AddressResponse resp = service.addAddress(1L, req);
//
//        assertTrue(resp.isDefault());
//    }
//
//    @Test
//    void testListAddresses_success() {
//        when(customers.findById(1L)).thenReturn(Optional.of(customer));
//        when(addresses.findByCustomer(customer)).thenReturn(Collections.singletonList(address));
//
//        List<AddressResponse> resp = service.listAddresses(1L);
//
//        assertEquals(1, resp.size());
//    }
//
//    @Test
//    void testSetDefaultAddress_success() {
//        when(customers.findById(1L)).thenReturn(Optional.of(customer));
//        when(addresses.findByIdAndCustomer(10L, customer)).thenReturn(Optional.of(address));
//        when(addresses.save(any(Address.class))).thenReturn(address);
//        when(addresses.findByCustomer(customer)).thenReturn(Collections.singletonList(address));
//
//        service.setDefaultAddress(1L, 10L);
//
//        assertTrue(address.isDefault());
//    }
//
//    @Test
//    void testSetDefaultAddress_alreadyDefault() {
//        address.setDefault(true);
//        when(customers.findById(1L)).thenReturn(Optional.of(customer));
//        when(addresses.findByIdAndCustomer(10L, customer)).thenReturn(Optional.of(address));
//
//        service.setDefaultAddress(1L, 10L);
//        assertTrue(address.isDefault()); // unchanged
//    }
//
//    @Test
//    void testUpdateAddress_success() {
//        AddressRequest req = new AddressRequest("X", "Y", "CityX", "StateX", "11111", "USA", false);
//
//        when(customers.findById(1L)).thenReturn(Optional.of(customer));
//        when(addresses.findByIdAndCustomer(10L, customer)).thenReturn(Optional.of(address));
//        when(addresses.save(any(Address.class))).thenReturn(address);
//
//        AddressResponse resp = service.updateAddress(1L, 10L, req);
//
//        assertEquals("X", resp.line1());
//    }
//
//    @Test
//    void testPatchAddress_success() {
//        Map<String, Object> updates = new HashMap<>();
//        updates.put("line1", "PatchL1");
//        updates.put("default", true);
//
//        when(customers.findById(1L)).thenReturn(Optional.of(customer));
//        when(addresses.findByIdAndCustomer(10L, customer)).thenReturn(Optional.of(address));
//        when(addresses.save(any(Address.class))).thenReturn(address);
//
//        AddressResponse resp = service.patchAddress(1L, 10L, updates);
//
//        assertEquals("PatchL1", resp.line1());
//    }
//
//    @Test
//    void testDeleteAddress_success() {
//        when(customers.findById(1L)).thenReturn(Optional.of(customer));
//        when(addresses.findByIdAndCustomer(10L, customer)).thenReturn(Optional.of(address));
//
//        service.deleteAddress(1L, 10L);
//
//        verify(addresses).delete(address);
//    }
//
//    @Test
//    void testUnsetOtherDefaults_changesOtherAddresses() {
//        Address addr2 = new Address();
//        addr2.setId(20L);
//        addr2.setCustomer(customer);
//        addr2.setDefault(true);
//
//        List<Address> list = Arrays.asList(address, addr2);
//
//        when(customers.findById(1L)).thenReturn(Optional.of(customer));
//        when(addresses.findByIdAndCustomer(10L, customer)).thenReturn(Optional.of(address)); // ✅ fix
//        when(addresses.findByCustomer(customer)).thenReturn(list);
//        when(addresses.save(any(Address.class))).thenReturn(addr2);
//
//        service.setDefaultAddress(1L, 10L);
//
//        assertFalse(addr2.isDefault());
//    }
//
//    // ===== ERROR PATHS =====
//
//    @Test
//    void testUpdatePhone_notFound() {
//        when(customers.findById(1L)).thenReturn(Optional.empty());
//        assertThrows(IllegalArgumentException.class, () -> service.updatePhone(1L, "12345"));
//    }
//
//    @Test
//    void testSuspendCustomer_notFound() {
//        when(customers.findById(1L)).thenReturn(Optional.empty());
//        assertThrows(IllegalArgumentException.class, () -> service.suspendCustomer(1L));
//    }
//
//    @Test
//    void testReactivateCustomer_notFound() {
//        when(customers.findById(1L)).thenReturn(Optional.empty());
//        assertThrows(IllegalArgumentException.class, () -> service.reactivateCustomer(1L));
//    }
//
//    @Test
//    void testAddAddress_customerNotFound() {
//        when(customers.findById(1L)).thenReturn(Optional.empty());
//        AddressRequest req = new AddressRequest("L1", "L2", "C", "S", "P", "Country", false);
//        assertThrows(IllegalArgumentException.class, () -> service.addAddress(1L, req));
//    }
//
//    @Test
//    void testListAddresses_customerNotFound() {
//        when(customers.findById(1L)).thenReturn(Optional.empty());
//        assertThrows(IllegalArgumentException.class, () -> service.listAddresses(1L));
//    }
//
//    @Test
//    void testSetDefaultAddress_notFound() {
//        when(customers.findById(1L)).thenReturn(Optional.of(customer));
//        when(addresses.findByIdAndCustomer(10L, customer)).thenReturn(Optional.empty());
//        assertThrows(IllegalArgumentException.class, () -> service.setDefaultAddress(1L, 10L));
//    }
//
//    @Test
//    void testUpdateAddress_addressNotFound() {
//        when(customers.findById(1L)).thenReturn(Optional.of(customer));
//        when(addresses.findByIdAndCustomer(10L, customer)).thenReturn(Optional.empty());
//        AddressRequest req = new AddressRequest("L1", "L2", "C", "S", "P", "Country", false);
//        assertThrows(IllegalArgumentException.class, () -> service.updateAddress(1L, 10L, req));
//    }
//
//    @Test
//    void testPatchAddress_addressNotFound() {
//        when(customers.findById(1L)).thenReturn(Optional.of(customer));
//        when(addresses.findByIdAndCustomer(10L, customer)).thenReturn(Optional.empty());
//        assertThrows(IllegalArgumentException.class, () -> service.patchAddress(1L, 10L, new HashMap<>()));
//    }
//
//    @Test
//    void testDeleteAddress_addressNotFound() {
//        when(customers.findById(1L)).thenReturn(Optional.of(customer));
//        when(addresses.findByIdAndCustomer(10L, customer)).thenReturn(Optional.empty());
//        assertThrows(IllegalArgumentException.class, () -> service.deleteAddress(1L, 10L));
//    }
//}



package com.example.customer.service;

import com.example.customer.domain.Address;
import com.example.customer.domain.Customer;
import com.example.customer.dto.AddressRequest;
import com.example.customer.dto.AddressResponse;
import com.example.customer.dto.CustomerResponse;
import com.example.customer.repository.AddressRepository;
import com.example.customer.repository.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;
    @Mock
    private AddressRepository addressRepository;

    @InjectMocks
    private CustomerService service;

    private Customer customer;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        customer = new Customer();
        customer.setId(1L);
        customer.setEmail("old@example.com");
        customer.setFullName("John Doe");
        customer.setPhone("12345");
        customer.setStatus(Customer.Status.ACTIVE);
    }

    // ---------------- CUSTOMER TESTS ----------------

    @Test
    void getCustomerById_found() {
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        CustomerResponse res = service.getCustomerById(1L);
        assertEquals("old@example.com", res.email());
    }

    @Test
    void getCustomerById_notFound() {
        when(customerRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> service.getCustomerById(1L));
    }

    @Test
    void updateEmail_success() {
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(customerRepository.existsByEmailIgnoreCase("new@example.com")).thenReturn(false);
        when(customerRepository.save(any(Customer.class))).thenAnswer(i -> i.getArgument(0));

        CustomerResponse res = service.updateEmail(1L, "new@example.com");
        assertEquals("new@example.com", res.email());
    }

    @Test
    void updateEmail_customerNotFound() {
        when(customerRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> service.updateEmail(1L, "x@example.com"));
    }

    @Test
    void updateEmail_duplicateEmail() {
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(customerRepository.existsByEmailIgnoreCase("dup@example.com")).thenReturn(true);
        assertThrows(IllegalArgumentException.class, () -> service.updateEmail(1L, "dup@example.com"));
    }

    @Test
    void updatePhone_success() {
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(customerRepository.save(any(Customer.class))).thenAnswer(i -> i.getArgument(0));

        CustomerResponse res = service.updatePhone(1L, "98765");
        assertEquals("98765", res.phone());
    }

    @Test
    void updatePhone_notFound() {
        when(customerRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> service.updatePhone(1L, "98765"));
    }

    @Test
    void suspendCustomer_success() {
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(customerRepository.save(any(Customer.class))).thenAnswer(i -> i.getArgument(0));
        CustomerResponse res = service.suspendCustomer(1L);
        assertEquals(Customer.Status.SUSPENDED, customer.getStatus());
    }

    @Test
    void suspendCustomer_notFound() {
        when(customerRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> service.suspendCustomer(1L));
    }

    @Test
    void reactivateCustomer_success() {
        customer.setStatus(Customer.Status.SUSPENDED);
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(customerRepository.save(any(Customer.class))).thenAnswer(i -> i.getArgument(0));
        CustomerResponse res = service.reactivateCustomer(1L);
        assertEquals(Customer.Status.ACTIVE, customer.getStatus());
    }

    @Test
    void reactivateCustomer_notFound() {
        when(customerRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> service.reactivateCustomer(1L));
    }

    // ---------------- ADDRESS TESTS ----------------

    @Test
    void addAddress_success_firstAddressBecomesDefault() {
        AddressRequest req = new AddressRequest("l1", "l2", "city", "st", "123", "IN", false);
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(addressRepository.countByCustomer(customer)).thenReturn(0L);
        when(addressRepository.save(any(Address.class))).thenAnswer(i -> {
            Address a = i.getArgument(0);
            a.setId(10L);
            return a;
        });
        AddressResponse res = service.addAddress(1L, req);
        assertTrue(res.isDefault());
    }

    @Test
    void addAddress_makeDefaultTrue() {
        AddressRequest req = new AddressRequest("l1", "l2", "city", "st", "123", "IN", true);
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(addressRepository.countByCustomer(customer)).thenReturn(1L);
        when(addressRepository.save(any(Address.class))).thenAnswer(i -> {
            Address a = i.getArgument(0);
            a.setId(11L);
            return a;
        });
        AddressResponse res = service.addAddress(1L, req);
        assertTrue(res.isDefault());
    }

    @Test
    void addAddress_customerNotFound() {
        when(customerRepository.findById(1L)).thenReturn(Optional.empty());
        AddressRequest req = new AddressRequest("a", "b", "c", "d", "123", "X", false);
        assertThrows(IllegalArgumentException.class, () -> service.addAddress(1L, req));
    }

    @Test
    void listAddresses_success() {
        Address addr = new Address();
        addr.setId(22L);
        addr.setCustomer(customer);
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(addressRepository.findByCustomer(customer)).thenReturn(List.of(addr));

        List<AddressResponse> res = service.listAddresses(1L);
        assertEquals(1, res.size());
    }

    @Test
    void listAddresses_notFound() {
        when(customerRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> service.listAddresses(1L));
    }

    @Test
    void setDefaultAddress_success() {
        Address addr = new Address();
        addr.setId(33L);
        addr.setCustomer(customer);
        addr.setDefault(false);

        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(addressRepository.findByIdAndCustomer(33L, customer)).thenReturn(Optional.of(addr));
        when(addressRepository.save(any(Address.class))).thenAnswer(i -> i.getArgument(0));

        service.setDefaultAddress(1L, 33L);
        assertTrue(addr.isDefault());
    }

    @Test
    void setDefaultAddress_alreadyDefault_noChange() {
        Address addr = new Address();
        addr.setId(34L);
        addr.setCustomer(customer);
        addr.setDefault(true);

        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(addressRepository.findByIdAndCustomer(34L, customer)).thenReturn(Optional.of(addr));

        service.setDefaultAddress(1L, 34L);
        assertTrue(addr.isDefault());
        verify(addressRepository, never()).save(addr);
    }

    @Test
    void setDefaultAddress_customerNotFound() {
        when(customerRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> service.setDefaultAddress(1L, 99L));
    }

    @Test
    void setDefaultAddress_addressNotFound() {
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(addressRepository.findByIdAndCustomer(99L, customer)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> service.setDefaultAddress(1L, 99L));
    }

    @Test
    void updateAddress_success() {
        Address addr = new Address();
        addr.setId(44L);
        addr.setCustomer(customer);
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(addressRepository.findByIdAndCustomer(44L, customer)).thenReturn(Optional.of(addr));
        when(addressRepository.save(any(Address.class))).thenAnswer(i -> i.getArgument(0));

        AddressRequest req = new AddressRequest("l1", "l2", "city", "st", "123", "IN", false);
        AddressResponse res = service.updateAddress(1L, 44L, req);
        assertEquals("l1", res.line1());
    }

    @Test
    void updateAddress_customerNotFound() {
        when(customerRepository.findById(1L)).thenReturn(Optional.empty());
        AddressRequest req = new AddressRequest("a","b","c","d","e","f",false);
        assertThrows(IllegalArgumentException.class, () -> service.updateAddress(1L, 44L, req));
    }

    @Test
    void updateAddress_addressNotFound() {
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(addressRepository.findByIdAndCustomer(44L, customer)).thenReturn(Optional.empty());
        AddressRequest req = new AddressRequest("a","b","c","d","e","f",false);
        assertThrows(IllegalArgumentException.class, () -> service.updateAddress(1L, 44L, req));
    }

    @Test
    void patchAddress_partialUpdate() {
        Address addr = new Address();
        addr.setId(55L);
        addr.setCustomer(customer);

        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(addressRepository.findByIdAndCustomer(55L, customer)).thenReturn(Optional.of(addr));
        when(addressRepository.save(any(Address.class))).thenAnswer(i -> i.getArgument(0));

        Map<String, Object> updates = new HashMap<>();
        updates.put("city", "NewCity");
        updates.put("default", true);

        AddressResponse res = service.patchAddress(1L, 55L, updates);
        assertEquals("NewCity", res.city());
        assertTrue(res.isDefault());
    }

    @Test
    void patchAddress_customerNotFound() {
        when(customerRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class,
                () -> service.patchAddress(1L, 55L, Map.of("city", "X")));
    }

    @Test
    void patchAddress_addressNotFound() {
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(addressRepository.findByIdAndCustomer(55L, customer)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class,
                () -> service.patchAddress(1L, 55L, Map.of("city", "X")));
    }

    @Test
    void deleteAddress_success() {
        Address addr = new Address();
        addr.setId(66L);
        addr.setCustomer(customer);

        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(addressRepository.findByIdAndCustomer(66L, customer)).thenReturn(Optional.of(addr));

        service.deleteAddress(1L, 66L);
        verify(addressRepository).delete(addr);
    }

    @Test
    void deleteAddress_customerNotFound() {
        when(customerRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> service.deleteAddress(1L, 66L));
    }

    @Test
    void deleteAddress_addressNotFound() {
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(addressRepository.findByIdAndCustomer(66L, customer)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> service.deleteAddress(1L, 66L));
    }
    @Test
    void addAddress_notFirstAndNotDefault() {
        AddressRequest req = new AddressRequest("l1", "l2", "city", "st", "123", "IN", false);
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(addressRepository.countByCustomer(customer)).thenReturn(1L);
        when(addressRepository.save(any(Address.class))).thenAnswer(i -> {
            Address a = i.getArgument(0);
            a.setId(12L);
            return a;
        });
        AddressResponse res = service.addAddress(1L, req);
        assertFalse(res.isDefault());
    }

    @Test
    void patchAddress_updateAllFields() {
        Address addr = new Address();
        addr.setId(77L);
        addr.setCustomer(customer);
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(addressRepository.findByIdAndCustomer(77L, customer)).thenReturn(Optional.of(addr));
        when(addressRepository.save(any(Address.class))).thenAnswer(i -> i.getArgument(0));

        Map<String, Object> updates = new HashMap<>();
        updates.put("line1", "L1");
        updates.put("line2", "L2");
        updates.put("city", "CityX");
        updates.put("state", "STX");
        updates.put("postalCode", "99999");
        updates.put("country", "CountryX");
        updates.put("default", true);

        AddressResponse res = service.patchAddress(1L, 77L, updates);

        assertEquals("L1", res.line1());
        assertEquals("L2", res.line2());
        assertEquals("CityX", res.city());
        assertEquals("STX", res.state());
        assertEquals("99999", res.postalCode());
        assertEquals("CountryX", res.country());
        assertTrue(res.isDefault());
    }

    @Test
    void patchAddress_noUpdates() {
        Address addr = new Address();
        addr.setId(78L);
        addr.setCustomer(customer);
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(addressRepository.findByIdAndCustomer(78L, customer)).thenReturn(Optional.of(addr));
        when(addressRepository.save(any(Address.class))).thenAnswer(i -> i.getArgument(0));

        AddressResponse res = service.patchAddress(1L, 78L, Collections.emptyMap());
        assertNotNull(res);
    }

    @Test
    void unsetOtherDefaults_changesOtherAddresses() {
        Address def1 = new Address();
        def1.setId(100L);
        def1.setCustomer(customer);
        def1.setDefault(true);

        Address keep = new Address();
        keep.setId(200L);
        keep.setCustomer(customer);
        keep.setDefault(true);

        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(addressRepository.findByIdAndCustomer(200L, customer)).thenReturn(Optional.of(keep));
        when(addressRepository.findByCustomer(same(customer)))
                .thenReturn(Arrays.asList(def1, keep));
        when(addressRepository.save(any(Address.class))).thenAnswer(i -> i.getArgument(0));

        service.setDefaultAddress(1L, 200L);

        assertFalse(def1.isDefault(), "Old default should be unset");
        assertTrue(keep.isDefault(), "New default should remain set");
    }
}
