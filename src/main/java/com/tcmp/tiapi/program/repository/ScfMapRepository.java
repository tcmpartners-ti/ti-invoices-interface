package com.tcmp.tiapi.program.repository;

import com.tcmp.tiapi.program.model.ScfMap;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ScfMapRepository extends JpaRepository<ScfMap, Long> {

}
