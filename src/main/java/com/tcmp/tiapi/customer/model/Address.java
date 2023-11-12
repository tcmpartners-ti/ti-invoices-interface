package com.tcmp.tiapi.customer.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Where;

import java.io.Serial;
import java.io.Serializable;

@Entity
@Table(name = "SX20LF")
@Where(clause = "ADDRTYPE = '1'") // Link to primary address only
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class Address implements Serializable {
  @Serial private static final long serialVersionUID = 88791827389774L;

  @EmbeddedId
  protected AddressId id;

  @Column(name = "SXCUS1", length = 20, insertable = false, updatable = false)
  private String customerMnemonic;

  @Column(name = "EMAIL", length = 128)
  private String customerEmail;

  @Column(name = "ADDRTYPE", insertable = false, updatable = false)
  private Integer type;
}
