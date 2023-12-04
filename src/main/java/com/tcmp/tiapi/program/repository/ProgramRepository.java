package com.tcmp.tiapi.program.repository;

import com.tcmp.tiapi.program.model.Program;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProgramRepository extends JpaRepository<Program, Long> {
  Optional<Program> findById(String programId);

  Page<Program> findAllByCustomerMnemonic(String customerMnemonic, Pageable pageable);
}
