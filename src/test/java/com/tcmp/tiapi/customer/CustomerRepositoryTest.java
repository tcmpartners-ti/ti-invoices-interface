package com.tcmp.tiapi.customer;

import com.tcmp.tiapi.customer.model.Customer;
import com.tcmp.tiapi.customer.model.CustomerId;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class CustomerRepositoryTest {
  @Autowired
  private CustomerRepository testedCustomerRepository;

  @AfterEach
  void tearDown() {
    testedCustomerRepository.deleteAll();
  }

  @Test
  void itShouldCheckIfCustomerExistsByMnemonic() {
    String mnemonic = "1733466420001";

    testedCustomerRepository.save(Customer.builder()
      .id(CustomerId.builder()
        .sourceBankingBusinessCode("SBB123")
        .mnemonic(mnemonic).build())
      .fullName("Customer 123")
      // Other fields ...
      .build());

    boolean actualExists = testedCustomerRepository.existsByIdMnemonic(mnemonic);

    assertThat(actualExists).isTrue();
  }

  @Test
  void itShouldCheckIfCustomerDoesntExistByMnemonic() {
    String nonExistingMnemonic = "123";

    boolean actualExists = testedCustomerRepository.existsByIdMnemonic(nonExistingMnemonic);

    assertThat(actualExists).isFalse();
  }
}
