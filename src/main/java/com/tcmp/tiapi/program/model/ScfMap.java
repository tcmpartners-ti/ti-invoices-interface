package com.tcmp.tiapi.program.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "SCFMAP")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class ScfMap {
  @Id
  @Column(name = "KEY97", nullable = false)
  private Long pk;

  @Column(name = "PROGRAMME", nullable = false)
  private Long programId;

  @Column(name = "CPARTY", nullable = false)
  private Long counterPartyId;

  @Column(name = "PROG_TYPE", nullable = false)
  private String programType;

  @Column(name = "SUBTYPECAT", length = 1)
  private String subtypeCat;

  @Column(name = "PARTY", nullable = false)
  private Long partyId;

  @Column(name = "LIMIT_AMT")
  private BigDecimal limitAmt;

  @Column(name = "LIMIT_CCY", length = 3)
  private String limitCcy;

  @Column(name = "ADVNCE_PCT")
  private BigDecimal advncePct;

  @Column(name = "MAXPERUNIT", length = 1)
  private String maxPerUnit;

  @Column(name = "MAXPERNUM")
  private Integer maxPerNum;
}
