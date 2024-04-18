package com.tcmp.tiapi.program.repository;

import com.tcmp.tiapi.program.model.Program;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ProgramRepository extends JpaRepository<Program, Long> {
  Optional<Program> findById(String programId);

  Page<Program> findAllByCustomerMnemonic(String customerMnemonic, Pageable pageable);

  @Query(
      """
    SELECT
      program
    FROM
      Program program
    LEFT JOIN CounterParty counterParty ON
      counterParty.programmePk = program.pk
    WHERE
      counterParty.role = 'S'
      AND counterParty.mnemonic = :sellerMnemonic""")
  Page<Program> findAllBySellerMnemonic(String sellerMnemonic, Pageable pageable);

  @Query(
      """
    SELECT
      program
    FROM
      Program program
    LEFT JOIN CounterParty counterParty ON
      counterParty.programmePk = program.pk
    LEFT JOIN Customer customer ON
      customer.id.mnemonic = counterParty.customerMnemonic
    WHERE
      counterParty.role = 'S'
      AND customer.number = :sellerCif""")
  Page<Program> findAllBySellerCif(String sellerCif, Pageable pageable);
}
