package com.tcmp.tiapi.customer.repository;

import com.tcmp.tiapi.customer.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, String> {
  Optional<Account> findByTypeAndCustomerMnemonic(String type, String customerMnemonic);
}
