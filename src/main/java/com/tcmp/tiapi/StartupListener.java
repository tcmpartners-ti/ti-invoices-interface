package com.tcmp.tiapi;

import jakarta.annotation.Nonnull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

/** This class is used for deploy verification. */
@Component
@Slf4j
public class StartupListener implements ApplicationListener<ApplicationReadyEvent> {
  @Value("${COMMIT:unknown}")
  private String commitHash;

  @Override
  public void onApplicationEvent(@Nonnull ApplicationReadyEvent event) {
    log.info("Application built with commit [{}].", commitHash);
  }
}
