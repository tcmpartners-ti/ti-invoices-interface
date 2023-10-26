package com.tcmp.tiapi.customer.repository;

import com.tcmp.tiapi.customer.model.CounterParty;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CounterPartyRepository extends JpaRepository<CounterParty, Long> {
  Page<CounterParty> findByProgrammePkAndRole(Long programmePk, Character role, Pageable page);
}
