package com.tcmp.tiapi.invoice.repository;

import com.tcmp.tiapi.invoice.model.EventExtension;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface EventExtensionRepository extends JpaRepository<EventExtension, Long> {
  @Query(
      value =
          """
      SELECT
        e.*
      FROM
        EXTEVENT e
      JOIN BASEEVENT b ON
        b.KEY97 = e.EVENT
      JOIN MASTER m ON
        m.KEY97 = b.MASTER_KEY
      WHERE
        m.MASTER_REF = :masterReference
      """,
      nativeQuery = true)
  Optional<EventExtension> findByMasterReference(@Param("masterReference") String masterReference);
}
