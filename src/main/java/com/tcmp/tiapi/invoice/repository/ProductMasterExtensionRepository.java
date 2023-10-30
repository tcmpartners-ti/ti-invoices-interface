package com.tcmp.tiapi.invoice.repository;


import com.tcmp.tiapi.invoice.model.ProductMasterExtension;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductMasterExtensionRepository extends JpaRepository<ProductMasterExtension, Long> {
  @Query("SELECT e.financeAccount FROM ProductMasterExtension e " +
         "WHERE e.masterId = (SELECT m.id FROM ProductMaster m WHERE m.masterReference = :masterReference)")
  Optional<String> findFinanceAccountByMasterReference(String masterReference);
}
