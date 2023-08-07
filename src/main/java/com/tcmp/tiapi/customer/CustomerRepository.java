package com.tcmp.tiapi.customer;

import com.tcmp.tiapi.customer.model.Customer;
import com.tcmp.tiapi.customer.model.CustomerId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, CustomerId> {
  boolean existsByIdMnemonic(String customerMnemonic);
}
