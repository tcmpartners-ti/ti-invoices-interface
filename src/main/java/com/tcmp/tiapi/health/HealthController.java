package com.tcmp.tiapi.health;

import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {
  @GetMapping("health")
  public Map<String, String> getHealthCheck() {
    return Map.of(
      "message", "ok"
    );
  }
}
