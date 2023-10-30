package com.tcmp.tiapi.program.model;

import com.tcmp.tiapi.shared.converter.DatabaseBooleanConverter;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "EXTPROGRAMME")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class ProgramExtension {
  @Id
  @Column(name = "PID", nullable = false)
  private String programmeId;

  @Column(name = "EXFINDAY")
  private Integer extraFinancingDays;

  @Column(name = "EXFINREQ")
  @Convert(converter = DatabaseBooleanConverter.class)
  private Boolean requiresExtraFinancing;
}
