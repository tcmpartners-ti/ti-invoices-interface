package com.tcmp.tiapi.customer.repository;

import com.tcmp.tiapi.customer.model.Customer;
import com.tcmp.tiapi.customer.model.CustomerId;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, CustomerId> {
  Optional<Customer> findFirstByIdMnemonic(String customerMnemonic);

  Optional<Customer> findFirstByNumber(String customerNumber);

  boolean existsByIdMnemonic(String customerMnemonic);

  @Query(
      value =
          """
          SELECT
            COUNT(*)
          FROM
            SCFMAP relation
          JOIN SCFCPARTY seller ON
            relation.CPARTY = seller.KEY97
          JOIN SCFCPARTY buyer ON
            relation.PARTY = buyer.KEY97
          WHERE
            relation.PROG_TYPE = 'B'
            AND seller.CUSTOMER = :sellerMnemonic
            AND buyer.CUSTOMER = :buyerMnemonic""",
      nativeQuery = true)
  int totalRelationsWithBuyer(String sellerMnemonic, String buyerMnemonic);
}
