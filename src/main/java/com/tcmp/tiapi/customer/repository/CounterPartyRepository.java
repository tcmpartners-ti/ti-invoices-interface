package com.tcmp.tiapi.customer.repository;

import com.tcmp.tiapi.customer.model.CounterParty;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CounterPartyRepository extends JpaRepository<CounterParty, Long> {
  List<CounterParty> findByCustomerMnemonicAndRole(String customerMnemonic, Character role);
}
