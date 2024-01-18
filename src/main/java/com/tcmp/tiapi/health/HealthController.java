package com.tcmp.tiapi.health;

import io.swagger.v3.oas.annotations.Hidden;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("health")
@Hidden
public class HealthController {
  private final HealthService healthService;

  @GetMapping
  public Map<String, String> getHealthCheck() {
    return Map.of("message", "ok");
  }

  @GetMapping("full")
  public Map<String, String> getFullHealthCheck() {
    return healthService.checkStatusFromSources();
  }
}
