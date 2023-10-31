package com.tcmp.tiapi.customer.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

@Table(name = "Account")
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Account {
  @Id
  @Column(name = "ACCT_KEY", nullable = false)
  private String id;

  @Column(name = "CUS_MNM", length = 20)
  private String customerMnemonic;

  @Column(name = "ACC_TYPE", length = 10)
  private String type;

  @Column(name = "EXT_ACCTNO", length = 34)
  private String externalAccountNumber;
}
