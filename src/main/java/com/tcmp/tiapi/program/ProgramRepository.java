package com.tcmp.tiapi.program;

import com.tcmp.tiapi.program.model.Program;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProgramRepository extends JpaRepository<Program, Long> {
    Optional<Program> findById(String programUuid);

    Page<Program> findAllByCustomerMnemonic(String customerMnemonic, Pageable pageable);
}
