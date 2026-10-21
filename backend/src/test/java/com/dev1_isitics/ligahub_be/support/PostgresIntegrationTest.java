package com.dev1_isitics.ligahub_be.support;

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Base of the integration tests: the whole application against a real PostgreSQL 16 started by
 * Testcontainers (the same image of the compose). The container is shared by every test class and
 * lives until the JVM exits, so the cached Spring context always points to a running database.
 * Skipped when no Docker daemon is available.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers(disabledWithoutDocker = true)
public abstract class PostgresIntegrationTest {

  @ServiceConnection
  static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16");

  static {
    if (DockerClientFactory.instance().isDockerAvailable()) {
      POSTGRES.start();
    }
  }
}
