package com.tcmp.tiapi.customer.model;

import com.tcmp.tiapi.shared.converter.DatabaseBooleanConverter;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "GFPF")
public class Customer {
  @EmbeddedId
  private CustomerId id;

  @Column(name = "GFCUN", length = 35)
  private String fullName;

  @Column(name = "GFCPNC", length = 12)
  private String number;

  @Column(name = "PRIME4SWFT")
  @Convert(converter = DatabaseBooleanConverter.class)
  private Boolean shouldUsePrimeAddressForSwift;

  @Column(name = "GFPCUS1", length = 20)
  private String oldMnemonic;

  @Column(name = "GFDAS", length = 15)
  private String defShortName;

  @Column(name = "GFCTP1", length = 8)
  private String type;

  @Column(name = "GFCUB")
  @Convert(converter = DatabaseBooleanConverter.class)
  private Boolean isBlocked;

  @Column(name = "GFCUC")
  @Convert(converter = DatabaseBooleanConverter.class)
  private Boolean isClosed;

  @Column(name = "GFCUD")
  @Convert(converter = DatabaseBooleanConverter.class)
  private Boolean isDeceased;

  @Column(name = "GFCUZ")
  @Convert(converter = DatabaseBooleanConverter.class)
  private Boolean isInactive;

  @Column(name = "GFACO", length = 10)
  private String officerCode;

  @Column(name = "GFCRF", length = 20)
  private String reference;

  @Column(name = "GFLNM", length = 2)
  private String languageCode;

  @Column(name = "GFCNAP", length = 2)
  private String parentCountryCode;

  @Column(name = "GFCNAR", length = 2)
  private String riskCountryCode;

  @Column(name = "GFCNAL", length = 2)
  private String residenceCountryCode;

  @Column(name = "GFMTB", length = 8)
  private String mailToBrancCode;

  @Column(name = "TICUSTLOC", length = 60)
  private String location;

  @Column(name = "GFBRNM", length = 8)
  private String branchCode;

  @Column(name = "GFCA2", length = 2)
  private String analysisCode;

  @Column(name = "GFGRP", length = 6)
  private String group;

  // NOT A DATE IN DB!!!!
  @Column(name = "GFDLM", precision = 7)
  private BigDecimal dateMaintained;

  @Column(name = "GFC201", length = 20)
  private String bankCode1;

  @Column(name = "GFC202", length = 20)
  private String bankCode2;

  @Column(name = "GFC101", length = 10)
  private String bankCode3;

  @Column(name = "GFC102", length = 10)
  private String bankCode4;

  // Values: Y, N, C
  @Column(name = "TIFCMF")
  private Character midasFacilityAllow;

  // Local o Host
  @Column(name = "SOURCE")
  private Character source;

  @Column(name = "CLR_NUMBER", length = 34)
  private String localClearingNumber;

  @Column(name = "DATE_DL", precision = 7)
  private BigDecimal dateDownloaded;

  @Column(name = "MNT_IN_BO")
  @Convert(converter = DatabaseBooleanConverter.class)
  private Boolean isMaintainedInBackOffice;

  // Fields that should not be used (Found in db docs).
  @Column(name = "GFCUS", length = 6)
  private String gfcus;

  @Column(name = "GFPCUS", length = 6)
  private String gfpcus;

  @Column(name = "GFPCLC", length = 3)
  private String gfpclc;

  @Column(name = "GFCLC", length = 3)
  private String gfclc;

  @Column(name = "GFCTP", length = 2)
  private String gfctp;

  @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
  @JoinColumn(name = "GFCUS1", referencedColumnName = "SXCUS1", insertable = false, updatable = false)
  private Address address;
}
