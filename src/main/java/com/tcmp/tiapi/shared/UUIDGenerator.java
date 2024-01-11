package com.tcmp.tiapi.shared;

import java.util.UUID;
import org.springframework.stereotype.Component;

/**
 * This class is used to provide an easier testing experience for both unit and integration tests.
 */
@Component
public class UUIDGenerator {
  public String getNewId() {
    return UUID.randomUUID().toString();
  }
}
