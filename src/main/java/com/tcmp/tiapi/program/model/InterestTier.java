package com.tcmp.tiapi.program.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import lombok.*;

@Entity
@Table(name = "INT_TIER")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class InterestTier {
  @Id
  @Column(name = "KEY97", nullable = false)
  private Long pk;

  @Column(name = "OWNER", nullable = false)
  private Long owner;

  @Column(name = "TIER_RATE", nullable = false)
  private BigDecimal rate;

  @Column(name = "TIER_NUM", nullable = false)
  private Integer number;

  @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
  @JoinColumn(name = "OWNER", referencedColumnName = "KEY29", insertable = false, updatable = false)
  private Interest interest;
}
