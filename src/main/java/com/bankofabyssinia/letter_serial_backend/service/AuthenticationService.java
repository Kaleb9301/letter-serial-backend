package com.bankofabyssinia.letter_serial_backend.service;


import io.jsonwebtoken.Claims;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
// import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class AuthenticationService {
    private static final Logger log = LoggerFactory.getLogger(AuthenticationService.class);

    // private final JwtUtil jwtUtil;

    // public AuthenticationService(JwtUtil jwtUtil) {
    //     this.jwtUtil = jwtUtil;
    // }

    /**
     * Retrieves the current authenticated user's email from JWT token.
     * Optimized with clear priority order.
     */
    public String getCurrentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            log.warn("No authentication found in security context");
            return null;
        }

        Object principal = authentication.getPrincipal();

        // Try to extract from JWT claims using your JwtUtil
        if (principal instanceof Claims claims) {
            // Priority 1: Check 'email' claim
            Object emailClaim = claims.get("email");
            if (isValidEmail(emailClaim)) {
                return emailClaim.toString().trim().toLowerCase();
            }

            // Priority 2: Check 'preferred_username' claim
            Object preferredUsername = claims.get("preferred_username");
            if (isValidEmail(preferredUsername)) {
                return preferredUsername.toString().trim().toLowerCase();
            }

            // Priority 3: Check 'upn' claim
            Object upn = claims.get("upn");
            if (isValidEmail(upn)) {
                return upn.toString().trim().toLowerCase();
            }

            // Priority 4: Check 'sub' claim (subject)
            Object sub = claims.get("sub");
            if (isValidEmail(sub)) {
                return sub.toString().trim().toLowerCase();
            }
        }

        // Fallback: Check authentication name
        String authName = authentication.getName();
        if (isValidEmail(authName)) {
            return authName.trim().toLowerCase();
        }

        log.debug("Could not extract email from authentication. Name: {}", authName);
        return null;
    }

    /**
     * Retrieves the current user's district ID from JWT claims.
     */
    public String getCurrentUserDistrictId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            throw new IllegalStateException("Authentication not found in context");
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof Claims claims) {
            // Try 'districtId' claim first
            Object districtIdClaim = claims.get("districtId");
            if (districtIdClaim != null) {
                return districtIdClaim.toString();
            }

            // Try alternative claim names if needed
            Object officeIdClaim = claims.get("officeId"); // Legacy support
            if (officeIdClaim != null) {
                return officeIdClaim.toString();
            }
        }

        throw new IllegalStateException("District ID claim missing for authenticated user");
    }

    /**
     * Retrieves the current user's district code from JWT claims.
     * Added for frontend serial number generation.
     */
    public String getCurrentUserDistrictCode() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            throw new IllegalStateException("Authentication not found in context");
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof Claims claims) {
            Object districtCodeClaim = claims.get("districtCode");
            if (districtCodeClaim != null) {
                return districtCodeClaim.toString();
            }
        }

        throw new IllegalStateException("District code claim missing for authenticated user");
    }

    /**
     * Retrieves the current user's branch ID from JWT claims.
     */
    public String getCurrentUserBranchId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return null;
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof Claims claims) {
            Object branchIdClaim = claims.get("branchId");
            if (branchIdClaim != null) {
                return branchIdClaim.toString();
            }

            // Legacy support for departmentId
            Object departmentIdClaim = claims.get("departmentId");
            if (departmentIdClaim != null) {
                return departmentIdClaim.toString();
            }
        }

        return null;
    }

    /**
     * Retrieves the current user's clean name from JWT token.
     */
    public String getCurrentUserName() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            throw new IllegalStateException("Authentication not found in context");
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof Claims claims) {
            // Try 'name' claim first
            Object nameClaim = claims.get("name");
            if (nameClaim != null) {
                return nameClaim.toString().trim();
            }

            // Fallback: extract name from email
            String email = getCurrentUserEmail();
            if (email != null) {
                return extractNameFromEmail(email);
            }
        }

        return "System User";
    }

    /**
     * Checks if the current user has a specific role.
     */
    public boolean hasRole(String role) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getAuthorities() == null) {
            return false;
        }

        return authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals(role));
    }

    /**
     * Checks if the current user is an admin.
     */
    public boolean isAdmin() {
        return hasRole("ROLE_ADMIN");
    }

    /**
     * Checks if the current user is a writer/secretary.
     */
    public boolean isWriter() {
        return hasRole("ROLE_SECRETARY") || hasRole("ROLE_WRITER");
    }

    /**
     * Validates if a string is a valid email address.
     */
    private boolean isValidEmail(Object emailObj) {
        if (emailObj == null) {
            return false;
        }

        String email = emailObj.toString().trim();
        return email.contains("@") && email.length() > 3;
    }

    /**
     * Extracts name from email (everything before @).
     */
    private String extractNameFromEmail(String email) {
        if (email == null || !email.contains("@")) {
            return "User";
        }

        String namePart = email.split("@")[0];
        if (namePart.isEmpty()) {
            return "User";
        }

        // Capitalize first letter
        return Character.toUpperCase(namePart.charAt(0)) +
                (namePart.length() > 1 ? namePart.substring(1) : "");
    }

    public void debugAuthentication() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            log.info("No authentication found");
            return;
        }

        log.info("Authentication principal class: {}", authentication.getPrincipal().getClass().getName());
        log.info("Authentication principal: {}", authentication.getPrincipal());
        log.info("Authentication authorities: {}", authentication.getAuthorities());
        log.info("Authentication name: {}", authentication.getName());
        log.info("Authentication details: {}", authentication.getDetails());

        // Check if principal is Claims
        if (authentication.getPrincipal() instanceof Claims claims) {
            log.info("Claims keys: {}", claims.keySet());
            log.info("Claims districtId: {}", claims.get("districtId"));
            log.info("Claims districtCode: {}", claims.get("districtCode"));
            log.info("Claims branchId: {}", claims.get("branchId"));
            log.info("Claims name: {}", claims.get("name"));
            log.info("Claims role: {}", claims.get("role"));
        }
    }

    /**
     * Gets all user info as a map for easy access.
     * Useful for logging or debugging.
     */
    public Map<String, Object> getCurrentUserInfo() {
        Map<String, Object> userInfo = new HashMap<>();

        try {
            userInfo.put("email", getCurrentUserEmail());
            userInfo.put("name", getCurrentUserName());
            userInfo.put("districtId", getCurrentUserDistrictId());
            userInfo.put("districtCode", getCurrentUserDistrictCode());
            userInfo.put("branchId", getCurrentUserBranchId());
            userInfo.put("isAdmin", isAdmin());
            userInfo.put("isWriter", isWriter());
        } catch (Exception e) {
            log.warn("Could not extract complete user info: {}", e.getMessage());
        }

        return userInfo;
    }
}