package com.tcmp.tiapi.program.model;

import jakarta.persistence.*;
import java.util.List;
import lombok.*;

@Entity
@Table(name = "INTERE59")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class Interest {
  @Id
  @Column(name = "KEY29", nullable = false)
  private Long pk;

  @Column(name = "PROGRAMME", nullable = false)
  private Long programId;

  @Column(name = "SCFMAP", nullable = false)
  private Long scfMap;

  @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, mappedBy = "owner")
  private List<InterestTier> tier;
}
