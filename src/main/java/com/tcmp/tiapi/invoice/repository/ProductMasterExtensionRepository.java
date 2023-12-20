package com.tcmp.tiapi.invoice.repository;

import com.tcmp.tiapi.invoice.model.ProductMasterExtension;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductMasterExtensionRepository
    extends JpaRepository<ProductMasterExtension, Long> {
  @Query(
      """
      SELECT e
        FROM ProductMasterExtension e
        LEFT JOIN ProductMaster p ON p.id = e.masterId
        WHERE p.masterReference = :masterReference
    """)
  Optional<ProductMasterExtension> findByMasterReference(
      @Param("masterReference") String masterReference);
}
