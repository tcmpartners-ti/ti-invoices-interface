package com.tcmp.tiapi.program.model;

import com.tcmp.tiapi.shared.converter.DatabaseBooleanConverter;
import jakarta.persistence.*;
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

  @Column(name = "OBSOLETE", nullable = false)
  @Convert(converter = DatabaseBooleanConverter.class)
  private Boolean isObsolete;

  @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
  @JoinColumn(
      name = "SCFMAP",
      referencedColumnName = "KEY97",
      insertable = false,
      updatable = false)
  private ScfMap map;
}
