package com.tcmp.tiapi.customer.model;

import com.tcmp.tiapi.program.model.ScfMap;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.*;

@Table(name = "SCFCPARTY")
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class CounterParty {
  @Id
  @Column(name = "KEY97", nullable = false)
  private Long id;

  @Column(name = "PROGRAMME")
  private Long programmePk;

  @Size(max = 20)
  @Column(name = "CPARTY", length = 20)
  private String mnemonic;

  @Column(name = "ROLE")
  private Character role;

  @Size(max = 8)
  @Column(name = "CUST_SBB", length = 8)
  private String customerSourceBankingBusinessCode;

  @Size(max = 20)
  @Column(name = "CUSTOMER", length = 20)
  private String customerMnemonic;

  @Size(max = 35)
  @Column(name = "CPARTYNAME", length = 35)
  private String name;

  @Size(max = 35)
  @Column(name = "SALUTATION", length = 35)
  private String salutation;

  @Size(max = 35)
  @Column(name = "CPARTYNA1", length = 35)
  private String nameAndAddressLine1;

  @Size(max = 35)
  @Column(name = "CPARTYNA2", length = 35)
  private String nameAndAddressLine2;

  @Size(max = 35)
  @Column(name = "CPARTYNA3", length = 35)
  private String nameAndAddressLine3;

  @Size(max = 35)
  @Column(name = "CPARTYNA4", length = 35)
  private String nameAndAddressLine4;

  @Size(max = 35)
  @Column(name = "CPARTYNA5", length = 35)
  private String nameAndAddressLine5;

  @Lob
  @Column(name = "CPARTYNAF")
  private String nameAndAddressFreeFormat;

  @Size(max = 15)
  @Column(name = "ZIP", length = 15)
  private String zip;

  @Size(max = 20)
  @Column(name = "PHONE", length = 20)
  private String phone;

  @Size(max = 20)
  @Column(name = "FAX", length = 20)
  private String fax;

  @Size(max = 20)
  @Column(name = "TELEX", length = 20)
  private String telex;

  @Size(max = 8)
  @Column(name = "TELEX_ANS", length = 8)
  private String telexAnswerBack;

  @Size(max = 128)
  @Column(name = "EMAIL", length = 128)
  private String email;

  @Size(max = 8)
  @Column(name = "BRANCH", length = 8)
  private String branchCode;

  @Size(max = 4)
  @Column(name = "CPARTYSWB", length = 4)
  private String swiftAddressSWBank;

  @Size(max = 2)
  @Column(name = "CPARTYCNAS", length = 2)
  private String cpartycnas;

  @Size(max = 2)
  @Column(name = "CPARTYSWL", length = 2)
  private String cpartyswl;

  @Size(max = 3)
  @Column(name = "CPARTYSWBR", length = 3)
  private String cpartyswbr;

  @Size(max = 35)
  @Column(name = "CPARTYSNA1", length = 35)
  private String cpartysna1;

  @Size(max = 35)
  @Column(name = "CPARTYSNA2", length = 35)
  private String cpartysna2;

  @Size(max = 35)
  @Column(name = "CPARTYSNA3", length = 35)
  private String cpartysna3;

  @Size(max = 35)
  @Column(name = "CPARTYSNA4", length = 35)
  private String cpartysna4;

  @Size(max = 35)
  @Column(name = "CPARTYSNA5", length = 35)
  private String cpartysna5;

  @Lob
  @Column(name = "CPARTYSNAF")
  private String cpartysnaf;

  @Size(max = 2)
  @Column(name = "COUNTRY", length = 2)
  private String country;

  @Size(max = 2)
  @Column(name = "CPARTYXM", length = 2)
  private String cpartyxm;

  @Size(max = 2)
  @Column(name = "LANGUAGE", length = 2)
  private String language;

  @Column(name = "STATUS")
  private Character status;

  @Column(name = "LIMIT_AMT", precision = 15)
  private BigDecimal limitAmt;

  @Size(max = 3)
  @Column(name = "LIMIT_CCY", length = 3)
  private String limitCcy;

  @Column(name = "TRANSLIT")
  private Character translit;

  @Column(name = "LAST_MAINT")
  private LocalDate lastMaint;

  @Column(name = "OBSOLETE")
  private Character obsolete;

  @Size(max = 20)
  @Column(name = "AUTOKEY", length = 20)
  private String autokey;

  @Column(name = "MNT_IN_BO")
  private Character mntInBo;

  @Column(name = "TYPEFLAG")
  private Integer typeflag;

  @Column(name = "TSTAMP")
  private Integer tstamp;

  @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
  @JoinColumn(
          name = "PROGRAMME",
          referencedColumnName = "PROGRAMME",
          insertable = false,
          updatable = false
  )
  @JoinColumn(
          name = "AUTOKEY",
          referencedColumnName = "CPARTY",
          insertable = false,
          updatable = false
  )
  private ScfMap scfMap;

}
