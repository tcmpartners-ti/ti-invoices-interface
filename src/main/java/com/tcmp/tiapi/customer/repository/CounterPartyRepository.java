package com.tcmp.tiapi.customer.repository;

import com.tcmp.tiapi.customer.model.CounterParty;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CounterPartyRepository extends JpaRepository<CounterParty, Long> {
  Optional<CounterParty> findByCustomerMnemonicAndRole(String customerMnemonic, Character role);
}
