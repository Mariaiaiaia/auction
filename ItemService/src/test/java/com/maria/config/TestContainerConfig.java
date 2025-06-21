package com.maria.config;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;

public class TestContainerConfig {
    @Autowired
    private DatabaseClient databaseClient;

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
                    
                    CREATE TABLE IF NOT EXISTS item (
                        id SERIAL PRIMARY KEY,
                        item_name VARCHAR(255) NOT NULL,
                        description TEXT,
                        image VARCHAR(255),
                        is_sold BOOLEAN NOT NULL,
                        auction_id BIGINT,
                        seller BIGINT NOT NULL
                    );
                """;

        databaseClient.sql(sql)
                .fetch()
                .rowsUpdated()
                .block();
    }
}
