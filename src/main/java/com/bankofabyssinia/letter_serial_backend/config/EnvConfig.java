package com.bankofabyssinia.letter_serial_backend.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;

@Configuration
public class EnvConfig {

    private static final Logger log = LoggerFactory.getLogger(EnvConfig.class);

    @Value("${server.port:8080}")
    private String serverPort;

    @Value("${server.servlet.context-path:/}")
    private String contextPath;

    @Value("${spring.application.name:LetterSerialSystem}")
    private String appName;

    @PostConstruct
    public void init() {
        try {
            // Minimal TLS configuration
            System.setProperty("jdk.tls.client.protocols", "TLSv1.2");

            // Log application startup info
            log.info("=========================================");
            log.info("Application: {}", appName);
            log.info("Running on port: {}", serverPort);
            log.info("Context path: {}", contextPath);
            log.info("Environment variables loaded successfully");
            log.info("=========================================");

        } catch (Exception ex) {
            log.error("Failed to initialize environment configuration", ex);
        }
    }
}