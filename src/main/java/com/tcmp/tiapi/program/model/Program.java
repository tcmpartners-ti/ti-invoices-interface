package com.tcmp.tiapi.program.model;

import com.tcmp.tiapi.program.converter.DatabaseProgramStatusConverter;
import com.tcmp.tiapi.shared.converter.DatabaseBooleanConverter;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "SCFPROGRAM")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Program {
  @Id
  @Column(name = "KEY97", nullable = false)
  private Long pk;

  @Column(name = "STATUS")
  @Convert(converter = DatabaseProgramStatusConverter.class)
  private ProgramStatus status;

  @Column(name = "ID", length = 35)
  private String id;

  @Column(name = "DESCR", length = 60)
  private String description;

  @Column(name = "CUST_SBB", length = 8)
  private String customerSourceBankingBusiness;

  @Column(name = "CUSTOMER", length = 20)
  private String customerMnemonic;

  @Column(name = "PROG_TYPE")
  private Character type;

  @Column(name = "SUBTYPECAT")
  private Character subTypeCategory;

  @Column(name = "AVAIL_AMT", precision = 15)
  private BigDecimal availableLimitAmount;

  @Column(name = "AVAIL_CCY", length = 3)
  private String availableLimitCurrencyCode;

  @Column(name = "LAST_MAINT")
  private LocalDate lastMaintainedAt;

  @Column(name = "AUTOKEY", length = 20)
  private String autokey;

  @Lob
  @Column(name = "NARRATIVE")
  private String narrative;

  @Column(name = "INSURANCE")
  private Long insurance;

  @Column(name = "OBSOLETE")
  @Convert(converter = DatabaseBooleanConverter.class)
  private Boolean isObsolete;

  // Is this used as a foreign key?
  @Column(name = "SCFPROSTYP", length = 10)
  private String programSubTypeId;

  @Column(name = "EXPIRYDATE")
  private LocalDate expiryDate;

  @Column(name = "STARTDATE")
  private LocalDate startDate;

  @Column(name = "SALEREF", length = 34)
  private String reference;

  @Column(name = "FINPRODTYP")
  private Long financeProductTypeKey;

  @Column(name = "UPLOADEDBY")
  private Character uploadedBy;

  @Column(name = "FINCEREQBY")
  private Character financeRequestedBy;

  @Column(name = "FINCE_DBT")
  private Character financeDebitParty;

  @Column(name = "FINCE_TO")
  private Character financeToParty;

  @Column(name = "BUYACCREQ")
  @Convert(converter = DatabaseBooleanConverter.class)
  private Boolean isBuyerAcceptanceRequired;

  @Column(name = "CREATEDATE")
  private LocalDate createdAt;

  @Column(name = "BHALF_BRN", length = 8)
  private String bhalfBrn;

  @Column(name = "PRNTGUAREX")
  @Convert(converter = DatabaseBooleanConverter.class)
  private Boolean hasParentGuarantor;

  @Column(name = "MNT_IN_BO")
  @Convert(converter = DatabaseBooleanConverter.class)
  private Boolean isMaintainedInBackOffice;

  // Not documented
  @Column(name = "TYPEFLAG")
  private Integer typeFlag;

  @Column(name = "TSTAMP")
  private Integer timeStamp;
}
