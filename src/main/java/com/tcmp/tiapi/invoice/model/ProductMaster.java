package com.tcmp.tiapi.invoice.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "MASTER")
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ProductMaster {
  @Id
  @Column(name = "KEY97", nullable = false)
  private Long id;

  @Column(name = "CTRCT_DATE")
  private LocalDate contractDate; // issueDate in invoice
}
