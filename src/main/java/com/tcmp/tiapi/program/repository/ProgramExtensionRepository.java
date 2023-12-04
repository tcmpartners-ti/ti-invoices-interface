package com.tcmp.tiapi.program.repository;

import com.tcmp.tiapi.program.model.ProgramExtension;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProgramExtensionRepository extends JpaRepository<ProgramExtension, String> {
  Optional<ProgramExtension> findByProgrammeId(String programId);
}
