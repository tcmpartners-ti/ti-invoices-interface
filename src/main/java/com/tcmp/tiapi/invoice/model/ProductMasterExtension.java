package com.tcmp.tiapi.invoice.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import lombok.*;

@Entity
@Table(name = "EXTMASTER")
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ProductMasterExtension {
  @Id
  @Column(name = "KEY29", nullable = false)
  private Long id;

  @Column(name = "FINACC")
  private String financeAccount;

  @Column(name = "MASTER", nullable = false)
  private Long masterId;

  @Column(name = "GAFOPEID", nullable = false)
  private String gafOperationId;

  @Column(name = "GAFINTRT", nullable = false)
  private BigDecimal gafInterestRate;

  @Column(name = "GAFDIAMT", nullable = false)
  private BigDecimal gafDisbursementAmount;

  @Column(name = "GAFFACTO", nullable = false)
  private BigDecimal gafFactor;

  @Column(name = "BGAFINTS", nullable = false)
  private BigDecimal buyerInterestsAmount;

  @Column(name = "SGAFINTS", nullable = false)
  private BigDecimal sellerInterestsAmount;

  @Column(name = "BSOLCAMT", nullable = false)
  private BigDecimal buyerSolcaAmount;

  @Column(name = "SSOLCAMT", nullable = false)
  private BigDecimal sellerSolcaAmount;

  @Column(name = "GAFAMORT", nullable = false)
  private String amortizations;
}
