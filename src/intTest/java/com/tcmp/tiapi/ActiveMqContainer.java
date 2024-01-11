package com.tcmp.tiapi;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.LogMessageWaitStrategy;
import org.testcontainers.utility.DockerImageName;

public class ActiveMqContainer<SELF extends ActiveMqContainer<SELF>>
    extends GenericContainer<SELF> {

  public static final String IMAGE = "rmohr/activemq";
  public static final String DEFAULT_TAG = "5.15.9-alpine";
  public static final Integer JMS_PORT = 61616;

  public ActiveMqContainer(String dockerImageName) {
    this(DockerImageName.parse(dockerImageName));
  }

  public ActiveMqContainer(DockerImageName dockerImageName) {
    super(dockerImageName);

    String fullImage = String.format("%s:%s", IMAGE, DEFAULT_TAG);
    dockerImageName.assertCompatibleWith(DockerImageName.parse(fullImage));
    this.waitStrategy =
        (new LogMessageWaitStrategy())
            .withRegEx(".*Connector ws started.*\\s")
            .withStartupTimeout(Duration.of(60L, ChronoUnit.SECONDS));

    this.addExposedPort(JMS_PORT);
  }

  public String brokerUrl() {
    return String.format("tcp://%s:%s", getHost(), this.getMappedPort(JMS_PORT));
  }
}
