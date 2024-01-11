package com.tcmp.tiapi;

import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.specification.RequestSpecification;
import jakarta.jms.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.activemq.command.ActiveMQQueue;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.mockserver.client.MockServerClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.MountableFile;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@Testcontainers
@ActiveProfiles("integration")
@RequiredArgsConstructor
@Slf4j
public abstract class AbstractIntegrationTest {
  protected static MockServerClient mockServerClient;
  protected RequestSpecification requestSpecification;
  protected Connection connection;
  protected Session session;

  @Autowired private ConnectionFactory connectionFactory;
  @LocalServerPort private Integer localPort;

  @Value("${ti.queue.fti.pub.name}")
  protected String ftiOutgoingQueue;

  @Value("${ti.queue.fti.reply.name}")
  protected String ftiIncomingReplyQueue;

  @Value("${ti.queue.ticc.sub.name}")
  protected String ticcIncomingQueue;

  static Network network = Network.newNetwork();

  @Container
  static PostgreSQLContainer<?> postgres =
      new PostgreSQLContainer<>("postgres:16.1-alpine3.19")
          .withExposedPorts(5432)
          .withNetwork(network)
          .withNetworkAliases("postgres")
          .withReuse(true)
          .withCopyFileToContainer(
              MountableFile.forClasspathResource("integration/initdb.sql"),
              "/docker-entrypoint-initdb.d/init.sql");

  @Container
  static GenericContainer<?> redis =
      new GenericContainer<>("redis:7.2.3-alpine")
          .waitingFor(Wait.forLogMessage(".*Ready to accept connections tcp.*", 1))
          .withExposedPorts(6379)
          .withNetwork(network)
          .withNetworkAliases("redis")
          .withReuse(true);

  @Container
  static ActiveMqContainer<?> activemq =
      new ActiveMqContainer<>("rmohr/activemq:5.15.9-alpine")
          .withExposedPorts(61616, 8161)
          .withNetwork(network)
          .withNetworkAliases("mq")
          .withReuse(true);

  @Container
  static GenericContainer<?> mockServer =
      new GenericContainer<>("mockserver/mockserver:5.15.0")
          .waitingFor(Wait.forLogMessage(".*started on port.*", 1))
          .withExposedPorts(1080)
          .withNetwork(network)
          .withNetworkAliases("mock-server")
          .withReuse(true)
          .withEnv("MOCKSERVER_INITIALIZATION_JSON_PATH", "/mock-server.json")
          .withCopyFileToContainer(
              MountableFile.forClasspathResource("integration/mock-server.json"),
              "/mock-server.json");

  @DynamicPropertySource
  public static void setupProperties(DynamicPropertyRegistry registry) {
    String mockServerUrl = String.format("http://localhost:%d", mockServer.getFirstMappedPort());

    registry.add("spring.datasource.url", postgres::getJdbcUrl);
    registry.add("spring.data.redis.host", redis::getHost);
    registry.add("spring.data.redis.port", redis::getFirstMappedPort);
    registry.add("spring.activemq.broker-url", activemq::brokerUrl);
    registry.add("bp.api-gee.base-url", () -> mockServerUrl);
  }

  @BeforeAll
  static void initMockServerClient() {
    mockServerClient = new MockServerClient("localhost", mockServer.getFirstMappedPort());
  }

  @BeforeEach
  void initJmsSession() throws JMSException {
    connection = connectionFactory.createConnection();
    connection.start();

    session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
  }

  @BeforeEach
  void resetMockServerClient() {
    mockServerClient.reset();
  }

  @BeforeEach
  void setUpAbstractIntegrationTest() {
    RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();

    requestSpecification =
        new RequestSpecBuilder()
            .setBaseUri(String.format("http://localhost:%d", localPort))
            .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .build();
  }

  protected void cleanQueue(ActiveMQQueue queue) throws JMSException {
    try (MessageConsumer consumer = session.createConsumer(queue)) {
      Message message;
      while ((message = consumer.receiveNoWait()) != null) {
        message.acknowledge();
      }
    }
  }
}
