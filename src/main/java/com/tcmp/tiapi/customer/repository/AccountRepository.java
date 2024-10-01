package com.tcmp.tiapi.customer.repository;

import com.tcmp.tiapi.customer.model.Account;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AccountRepository extends JpaRepository<Account, String> {
  Optional<Account> findByTypeAndCustomerMnemonic(String type, String customerMnemonic);

  @Query("SELECT a FROM Account a WHERE a.customerMnemonic = :customerMnemonic AND a.type = 'CA' AND a.sequenceNumber = " +
          "(SELECT MAX(a2.sequenceNumber) FROM Account a2 WHERE a2.customerMnemonic = :customerMnemonic AND a2.type = 'CA')")
  Account findAccountWithMaxSequenceNumberByCustomerMnemonic(@Param("customerMnemonic") String customerMnemonic);
}
