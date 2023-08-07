package com.tcmp.tiapi.customer.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.Hibernate;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Embeddable
public class CustomerId implements Serializable {
    @Serial
    private static final long serialVersionUID = 7030907852707213599L;

    @Column(name = "GFCUS1_SBB", nullable = false, length = 8)
    private String sourceBankingBusinessCode;

    @Column(name = "GFCUS1", nullable = false, length = 20)
    private String mnemonic;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        CustomerId entity = (CustomerId) o;
        return Objects.equals(this.mnemonic, entity.mnemonic)
                && Objects.equals(this.sourceBankingBusinessCode, entity.sourceBankingBusinessCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mnemonic, sourceBankingBusinessCode);
    }
}
