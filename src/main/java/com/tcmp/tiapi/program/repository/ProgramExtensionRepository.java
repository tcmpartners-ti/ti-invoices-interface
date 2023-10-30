package com.tcmp.tiapi.program.repository;

import com.tcmp.tiapi.program.model.ProgramExtension;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProgramExtensionRepository extends JpaRepository<ProgramExtension, String> {
  Optional<ProgramExtension> findByProgrammeId(String programId);
}
