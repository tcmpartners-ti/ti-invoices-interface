package com.tcmp.tiapi.customer.repository;

import com.tcmp.tiapi.customer.model.Customer;
import com.tcmp.tiapi.customer.model.CustomerId;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, CustomerId> {
  Optional<Customer> findFirstByIdMnemonic(String customerMnemonic);

  boolean existsByIdMnemonic(String customerMnemonic);

  boolean existsByNumber(String customerNumber);
}
