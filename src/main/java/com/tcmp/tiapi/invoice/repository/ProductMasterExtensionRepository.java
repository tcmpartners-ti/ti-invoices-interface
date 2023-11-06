package com.tcmp.tiapi.invoice.repository;


import com.tcmp.tiapi.invoice.model.ProductMasterExtension;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductMasterExtensionRepository extends JpaRepository<ProductMasterExtension, Long> {
  Optional<ProductMasterExtension> findByMasterId(Long masterId);

  @Query("""
      SELECT e
        FROM ProductMasterExtension e
        WHERE e.masterId = (
          SELECT p.id
          FROM ProductMaster p
          WHERE
            p.masterReference = :masterReference
      )
    """)
  Optional<ProductMasterExtension> findByMasterReference(@Param("masterReference") String masterReference);
}
