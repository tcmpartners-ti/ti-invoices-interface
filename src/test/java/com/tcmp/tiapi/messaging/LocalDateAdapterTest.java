package com.tcmp.tiapi.messaging;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class LocalDateAdapterTest {
  private LocalDateAdapter localDateAdapter;

  @BeforeEach
  void setUp() {
    localDateAdapter = new LocalDateAdapter();
  }

  @Test
  void unmarshal() {
    var date = "2023-11-13" ;
    var expectedLocalDate = LocalDate.of(2023, 11, 13);

    var actualLocalDate = localDateAdapter.unmarshal(date);

    assertEquals(expectedLocalDate, actualLocalDate);
  }

  @Test
  void marshal() {
    var localDate = LocalDate.of(2023, 11, 13);
    var expectedDate = "2023-11-13" ;

    var actualDate = localDateAdapter.marshal(localDate);

    assertEquals(expectedDate, actualDate);
  }
}
