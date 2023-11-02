package com.tcmp.tiapi.customer.model;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;

@Entity
@Table(name = "SX20LF")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Address implements Serializable {
  @Serial private static final long serialVersionUID = 88791827389774L;

  @EmbeddedId
  protected AddressId id;

  @Column(name = "SXCUS1", length = 20, insertable = false, updatable = false)
  private String customerMnemonic;

  @Column(name = "EMAIL", length = 128)
  private String customerEmail;
}
