package com.tcmp.tiapi.schedule.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import lombok.*;

@Entity
@Table(
    name = "SCHEDULED_MESSAGE",
    indexes = {
      @Index(name = "status", columnList = "STATUS"),
      @Index(name = "deliverOn", columnList = "DELIVER_ON")
    })
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ScheduledMessage {
  @Id
  @Column(name = "ID", length = 36, nullable = false)
  private String id;

  @Column(name = "STATUS", nullable = false)
  @Enumerated(EnumType.STRING)
  private MessageStatus status;

  @Column(name = "DELIVER_ON", nullable = false)
  private LocalDate deliverOn;

  @Column(name = "ORIGINAL_MESSAGE", columnDefinition = "NVARCHAR(MAX)")
  private String originalMessage;
}
