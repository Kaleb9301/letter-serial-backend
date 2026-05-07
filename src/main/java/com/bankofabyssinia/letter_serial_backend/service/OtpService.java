package com.bankofabyssinia.letter_serial_backend.service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class OtpService {

    private final SecureRandom random = new SecureRandom();
    private static final int OTP_EXPIRATION_MINUTES = 5;

    @Value("${app.development-mode:false}")
    private boolean developmentMode;

    private final Cache<String, OtpEntry> otpCache;
    private final EmailService emailService;

    public OtpService(EmailService emailService) {
        this.emailService = emailService;
        this.otpCache = Caffeine.newBuilder()
                .expireAfterWrite(OTP_EXPIRATION_MINUTES, TimeUnit.MINUTES)
                .build();
    }

    @Data
    public static class OtpEntry {
        private final String userEmail;
        private final String otp;
        private final LocalDateTime expiry;
        private String otpSessionId;
        private String deviceFingerprint;
        private boolean emailSent = false;
    }

    /**
     * Generate real OTP + send email (only called when user actually exists)
     */
    public OtpEntry generateOtp(String userEmail, String toEmail, String otpSessionId, String deviceFingerprint) {
        log.info("[OTP] Generating real OTP for {}", userEmail);

        String otp = String.format("%06d", random.nextInt(1_000_000));
        LocalDateTime expiry = LocalDateTime.now().plusMinutes(OTP_EXPIRATION_MINUTES);

        String fpHash = deviceFingerprint == null ? "" : DigestUtils.sha256Hex(deviceFingerprint);

        OtpEntry entry = new OtpEntry(userEmail, otp, expiry);
        entry.setOtpSessionId(otpSessionId);
        entry.setDeviceFingerprint(fpHash);

        boolean emailSent = sendOtpSync(userEmail, toEmail, otp);
        entry.setEmailSent(emailSent);

        if (!emailSent) {
            log.error("[OTP] Failed to send OTP email to {}", userEmail);
            return null; // important: don't store if email failed
        }

        otpCache.put(userEmail, entry);
        return entry;
    }

    /**
     * Dummy OTP – used when user does not exist (timing attack protection)
     */
    public OtpEntry generateDummyOtp() {
        log.debug("[OTP] Generating dummy OTP (no email sent)");
        OtpEntry dummy = new OtpEntry("dummy@invalid", "000000", LocalDateTime.now().minusMinutes(10));
        dummy.setOtpSessionId(UUID.randomUUID().toString());
        dummy.setDeviceFingerprint(DigestUtils.sha256Hex("dummy"));
        dummy.setEmailSent(false);
        return dummy;
    }

    private boolean sendOtpSync(String userEmail, String toEmail, String otp) {
        try {
            Map<String, Object> vars = Map.of(
                    "otp", otp,
                    "expiryMinutes", OTP_EXPIRATION_MINUTES,
                    "bankName", "Bank of Abyssinia");
            boolean success = emailService.sendEmailViaMicroservice(
                    toEmail, "Your OTP Code", "email/otp-email", vars);

            if (!success) {
                log.error("[OTP] Email service returned failure for {}", userEmail);
                return false;
            }

            log.info("[OTP] OTP email sent to {}", userEmail);
            return true;
        } catch (Exception e) {
            log.error("[OTP] Failed to send OTP to {}: {}", userEmail, e.getMessage(), e);
            return false;
        }
    }

    @Async
    public CompletableFuture<Boolean> sendOtpAsync(String userEmail, String email, String otp) {
        return CompletableFuture.completedFuture(sendOtpSync(userEmail, email, otp));
    }

    /**
     * Validate OTP – used during reset verification (3 arguments)
     */
    public boolean validateOtp(String userEmail, String otp, String otpSessionId) {
        if (developmentMode && "111111".equals(otp)) {
            log.warn("[OTP] Development mode OTP used: 111111");
            return true;
        }

        OtpEntry entry = otpCache.getIfPresent(userEmail);
        if (entry == null) {
            return false;
        }

        if (!entry.getOtp().equals(otp)) {
            return false;
        }

        if (!otpSessionId.equals(entry.getOtpSessionId())) {
            return false;
        }

        if (entry.getExpiry().isBefore(LocalDateTime.now())) {
            otpCache.invalidate(userEmail);
            return false;
        }

        // Do NOT invalidate here – let completePasswordReset call invalidateOtp
        return true;
    }

    /**
     * Remove OTP after successful password change
     */
    public void invalidateOtp(String userEmail, String otpSessionId) {
        OtpEntry entry = otpCache.getIfPresent(userEmail);
        if (entry != null && otpSessionId.equals(entry.getOtpSessionId())) {
            otpCache.invalidate(userEmail);
            log.debug("[OTP] Invalidated OTP for {}", userEmail);
        }
    }

    public boolean wasEmailSent(String userEmail) {
        OtpEntry entry = otpCache.getIfPresent(userEmail);
        return entry != null && entry.isEmailSent();
    }

    @Scheduled(fixedRateString = "${otp.cleanup.fixedRate:600000}") // 10 minutes
    public void cleanupExpiredOtps() {
        otpCache.cleanUp();
        log.debug("[OTP] Cache cleanup executed");
    }
}