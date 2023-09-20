package com.tcmp.tiapi.customer.repository;

import com.tcmp.tiapi.customer.model.CounterParty;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface CounterPartyRepository extends JpaRepository<CounterParty, Long> {
  Optional<CounterParty> findByProgrammePkAndMnemonicAndRole(Long programmePk, String mnemonic, Character role);

  List<CounterParty> findByIdIn(List<Long> counterPartiesIds);

  Page<CounterParty> findByProgrammePkAndRole(Long programmePk, Character role, Pageable page);

  @Query("SELECT DISTINCT c.id FROM CounterParty c WHERE c.customerMnemonic = :customerMnemonic AND c.role = :role")
  List<Long> findUniqueIdsByCustomerMnemonicAndRole(String customerMnemonic, Character role);
}
