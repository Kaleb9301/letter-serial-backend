package com.bankofabyssinia.letter_serial_backend.config;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;

import com.bankofabyssinia.letter_serial_backend.repository.UserRepository;

@Configuration
@EnableScheduling
public class PasswordResetCleanupConfig {

    private static final Logger log = LoggerFactory.getLogger(PasswordResetCleanupConfig.class);

    private final UserRepository userRepository;

    public PasswordResetCleanupConfig(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Scheduled(cron = "0 */15 * * * ?") // every 15 minutes
    @Transactional
    public void cleanupExpiredResetSessions() {
        try {
            int count = userRepository.deleteExpiredPasswordResetSessions(LocalDateTime.now());
            if (count > 0) {
                log.info("Cleaned up {} expired password reset sessions", count);
            }
        } catch (Exception e) {
            log.error("Error during expired reset session cleanup", e);
        }
    }
}