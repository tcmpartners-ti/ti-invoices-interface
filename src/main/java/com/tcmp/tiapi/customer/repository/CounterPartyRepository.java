package com.tcmp.tiapi.customer.repository;

import com.tcmp.tiapi.customer.model.CounterParty;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface CounterPartyRepository extends JpaRepository<CounterParty, Long> {
  Page<CounterParty> findByProgrammePkAndRole(Long programmePk, Character role, Pageable page);

  @Query(
      "SELECT COUNT(cp) > 0 FROM CounterParty cp WHERE cp.role = 'S' AND cp.mnemonic = :mnemonic")
  boolean counterPartyIsSeller(String mnemonic);

  @Query(
      "SELECT COUNT(cp) > 0 FROM CounterParty cp WHERE cp.role = 'B' AND cp.mnemonic = :mnemonic")
  boolean counterPartyIsBuyer(String mnemonic);

  @Query("SELECT cp.id FROM CounterParty cp WHERE cp.role = 'S' and cp.mnemonic = :mnemonic")
  Set<Long> findSellerIdsByMnemonic(String mnemonic);
}
