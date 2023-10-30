package com.tcmp.tiapi.invoice.model;

import jakarta.persistence.*;
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

  @Column(name = "MASTER")
  private Long masterId;
}
