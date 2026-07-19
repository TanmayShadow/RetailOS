package com.example.RetailOS.customer.service;

import com.example.RetailOS.customer.dto.request.CreateCustomerRequest;
import com.example.RetailOS.customer.dto.request.UpdateCustomerRequest;
import com.example.RetailOS.customer.dto.response.CustomerResponse;
import com.example.RetailOS.customer.entity.Customer;
import com.example.RetailOS.customer.repository.CustomerRepository;
import com.example.RetailOS.platform.exception.ResourceAlreadyExistsException;
import com.example.RetailOS.platform.exception.ResourceNotFoundException;
import com.example.RetailOS.platform.security.service.CurrentUserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final CurrentUserService currentUserService;

    /**
     * Optional search matches name (case-insensitive, contains) or, when
     * the query looks like digits, the phone number.
     */
    public List<CustomerResponse> getCustomers(String search) {
        UUID shopId = currentUserService.getCurrentShopId();

        List<Customer> customers;
        if (search == null || search.isBlank()) {
            customers = customerRepository.findAllByShopIdOrderByNameAsc(shopId);
        } else if (search.trim().matches("[0-9+\\-() ]+")) {
            customers = customerRepository
                    .findAllByShopIdAndPhoneContainingOrderByNameAsc(shopId, search.trim());
        } else {
            customers = customerRepository
                    .findAllByShopIdAndNameContainingIgnoreCaseOrderByNameAsc(shopId, search.trim());
        }
        return customers.stream().map(CustomerResponse::from).toList();
    }

    public CustomerResponse getCustomer(UUID id) {
        return CustomerResponse.from(
                requireCustomer(id, currentUserService.getCurrentShopId()));
    }

    @Transactional
    public CustomerResponse createCustomer(CreateCustomerRequest request) {

        UUID shopId = currentUserService.getCurrentShopId();
        String phone = normalizePhone(request.getPhone());

        if (phone != null && customerRepository.existsByShopIdAndPhone(shopId, phone)) {
            throw new ResourceAlreadyExistsException(
                    "A customer with this phone number already exists.");
        }

        Customer customer = Customer.builder()
                .shopId(shopId)
                .name(request.getName().trim())
                .phone(phone)
                .build();

        return CustomerResponse.from(customerRepository.save(customer));
    }

    @Transactional
    public CustomerResponse updateCustomer(UUID id, UpdateCustomerRequest request) {

        UUID shopId = currentUserService.getCurrentShopId();
        Customer customer = requireCustomer(id, shopId);

        if (request.getName() != null) {
            customer.setName(request.getName().trim());
        }
        if (request.getPhone() != null) {
            String phone = normalizePhone(request.getPhone());
            if (phone != null
                    && customerRepository.existsByShopIdAndPhoneAndIdNot(shopId, phone, id)) {
                throw new ResourceAlreadyExistsException(
                        "A customer with this phone number already exists.");
            }
            customer.setPhone(phone);
        }

        return CustomerResponse.from(customerRepository.save(customer));
    }

    /**
     * Past invoices and payments keep existing — their DB FKs are
     * ON DELETE SET NULL, so history survives as walk-in records.
     */
    @Transactional
    public void deleteCustomer(UUID id) {
        Customer customer = requireCustomer(id, currentUserService.getCurrentShopId());
        customerRepository.delete(customer);
    }

    private String normalizePhone(String phone) {
        if (phone == null || phone.isBlank()) {
            return null;
        }
        return phone.trim();
    }

    private Customer requireCustomer(UUID id, UUID shopId) {
        return customerRepository.findByIdAndShopId(id, shopId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Customer not found: " + id));
    }
}
