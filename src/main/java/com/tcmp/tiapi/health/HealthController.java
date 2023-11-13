package com.tcmp.tiapi.health;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class HealthController {
  @GetMapping("health")
  public Map<String, String> getHealthCheck() {
    return Map.of(
      "message", "ok"
    );
  }
}
