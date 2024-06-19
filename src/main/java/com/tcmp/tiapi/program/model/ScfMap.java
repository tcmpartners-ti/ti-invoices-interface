package com.tcmp.tiapi.program.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

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

  @Column(name = "PARTY", nullable = false)
  private Long partyId;

  @Column(name = "PROG_TYPE", nullable = false)
  private String programType;
}
