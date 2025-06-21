package com.maria.config;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;

public abstract class TestContainerConfig {
    @Autowired
    private DatabaseClient databaseClient;

    static Network network = Network.newNetwork();

    @Container
    private static final PostgreSQLContainer<?> POSTGRESQL_CONTAINER =
            new PostgreSQLContainer<>("postgres:latest")
                    .withDatabaseName("testdb")
                    .withUsername("postres")
                    .withPassword("1234");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.r2dbc.url",
                () -> "r2dbc:postgresql://" + POSTGRESQL_CONTAINER.getHost() +
                        ":" + POSTGRESQL_CONTAINER.getMappedPort(5432) + "/testdb");
        registry.add("spring.r2dbc.username", POSTGRESQL_CONTAINER::getUsername);
        registry.add("spring.r2dbc.password", POSTGRESQL_CONTAINER::getPassword);
    }

    @PostConstruct
    public void initializeDatabase() {
        String sql = """
                    CREATE SCHEMA IF NOT EXISTS auction;
                    
                    CREATE TABLE IF NOT EXISTS usr (
                        id SERIAL PRIMARY KEY,
                        first_name VARCHAR(50) NOT NULL,
                        last_name VARCHAR(50) NOT NULL,
                        password VARCHAR(255) NOT NULL,
                        email VARCHAR(100) UNIQUE NOT NULL,
                        role VARCHAR(20) NOT NULL
                    );
                """;

        databaseClient.sql(sql)
                .fetch()
                .rowsUpdated()
                .block();
    }
}
