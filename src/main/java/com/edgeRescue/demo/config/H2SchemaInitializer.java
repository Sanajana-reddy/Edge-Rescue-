package com.edgeRescue.demo.config;

import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class H2SchemaInitializer {

    @Bean
    ApplicationRunner ensureEmergencyTicketCoordinates(JdbcTemplate jdbcTemplate) {
        return args -> {
            jdbcTemplate.execute("ALTER TABLE emergency_tickets ADD COLUMN IF NOT EXISTS latitude DOUBLE DEFAULT 0 NOT NULL");
            jdbcTemplate.execute("ALTER TABLE emergency_tickets ADD COLUMN IF NOT EXISTS longitude DOUBLE DEFAULT 0 NOT NULL");
        };
    }
}
