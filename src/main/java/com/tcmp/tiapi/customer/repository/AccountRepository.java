package com.tcmp.tiapi.customer.repository;

import com.tcmp.tiapi.customer.model.Account;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRepository extends JpaRepository<Account, String> {
  Optional<Account> findByTypeAndCustomerMnemonic(String type, String customerMnemonic);
}
