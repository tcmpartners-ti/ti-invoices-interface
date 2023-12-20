package com.tcmp.tiapi.invoice.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Table(name = "EXTEVENT")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class EventExtension {
  @Id
  @Column(name = "KEY29", nullable = false)
  private Long id;

  @Column(name = "FINSEACC")
  private String financeSellerAccount;
}
