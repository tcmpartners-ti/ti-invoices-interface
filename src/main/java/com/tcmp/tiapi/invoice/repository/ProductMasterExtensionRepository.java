package com.tcmp.tiapi.invoice.repository;


import com.tcmp.tiapi.invoice.model.ProductMasterExtension;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductMasterExtensionRepository extends JpaRepository<ProductMasterExtension, Long> {
  Optional<ProductMasterExtension> findByMasterId(Long masterId);
}
