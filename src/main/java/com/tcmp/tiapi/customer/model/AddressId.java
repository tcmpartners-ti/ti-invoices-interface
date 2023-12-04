package com.tcmp.tiapi.customer.model;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;
import org.hibernate.Hibernate;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class AddressId implements Serializable {
  @Serial private static final long serialVersionUID = 98098981029381218L;

  @Basic(optional = false)
  @Column(name = "SXCUS1_SBB", length = 8)
  private String sourceBankingBusinessCode;

  @Basic(optional = false)
  @Column(name = "SXCUS1", length = 20)
  private String customerMnemonic;

  @Basic(optional = false)
  @Column(name = "ADDRTYPE")
  private Integer type;

  @Basic(optional = false)
  @Column(name = "SEQUENCE")
  private Integer sequence;

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
    AddressId entity = (AddressId) o;
    return Objects.equals(this.customerMnemonic, entity.customerMnemonic)
        && Objects.equals(this.sourceBankingBusinessCode, entity.sourceBankingBusinessCode)
        && Objects.equals(this.type, entity.type)
        && Objects.equals(this.sequence, entity.sequence);
  }

  @Override
  public int hashCode() {
    return Objects.hash(sourceBankingBusinessCode, customerMnemonic, type, sequence);
  }
}
