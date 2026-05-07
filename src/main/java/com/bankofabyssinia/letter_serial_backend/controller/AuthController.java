package com.bankofabyssinia.letter_serial_backend.controller;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bankofabyssinia.letter_serial_backend.dto.Request.LdapLoginRequest;
import com.bankofabyssinia.letter_serial_backend.dto.Request.LoginCredentials;
import com.bankofabyssinia.letter_serial_backend.dto.Request.RefreshTokenRequest;
import com.bankofabyssinia.letter_serial_backend.dto.Response.ApiResponse;
import com.bankofabyssinia.letter_serial_backend.dto.Response.LdapLoginResponse;
import com.bankofabyssinia.letter_serial_backend.dto.Response.UserToken;
import com.bankofabyssinia.letter_serial_backend.entity.User;
import com.bankofabyssinia.letter_serial_backend.repository.UserRepository;
import com.bankofabyssinia.letter_serial_backend.service.AuthService;
import com.bankofabyssinia.letter_serial_backend.service.JwtUtil;
import com.bankofabyssinia.letter_serial_backend.service.JwtUtil.TokenDetails;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;

@RestController
@Validated
@RequestMapping("/auth")
public class AuthController extends BaseController {

   

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginCredentials credentials) {
        Optional<User> maybeUser = userRepository.findByEmail(credentials.getEmail());
        if (maybeUser.isEmpty()) {
            return unauthorized("Invalid email or password");
        }

        User user = maybeUser.get();

        if (!user.isActive()) {
            return forbidden("User account is disabled");
        }

        if (!passwordEncoder.matches(credentials.getPassword(), user.getPassword())) {
            return unauthorized("Invalid email or password");
        }

        if (user.getDistrict() == null) {
            return unauthorized("User is not assigned to a district");
        }

        LocalDateTime now = LocalDateTime.now();

        // Single active session enforcement
        boolean hasActiveSession = user.getActiveSessionId() != null &&
                user.getSessionExpiresAt() != null &&
                user.getSessionExpiresAt().isAfter(now);

        if (hasActiveSession && !credentials.isForceLogin()) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Active session detected in another browser. " +
                    "Do you want to log out from all other sessions?");
            response.put("requiresForceLogin", true);
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
        }

        // Force logout of other sessions if requested
        if (credentials.isForceLogin()) {
            clearSessions(user);
        }

        Map<String, Object> claims = buildClaims(user);

        TokenDetails accessToken = jwtUtil.generateAccessToken(user.getEmail(), claims);
        TokenDetails refreshToken = jwtUtil.generateRefreshToken(user.getEmail(), claims);

        // Update user session tracking
        user.setActiveSessionId(accessToken.tokenId());
        user.setSessionExpiresAt(accessToken.expiresAt().toLocalDateTime());
        user.setRefreshSessionId(refreshToken.tokenId());
        user.setRefreshExpiresAt(refreshToken.expiresAt().toLocalDateTime());
        user.setLastLoginAt(now);

        userRepository.save(user);

        return ResponseEntity.ok(buildCompleteUserToken(user, accessToken, refreshToken));
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        String providedToken = request.getRefreshToken();

        if (!jwtUtil.validateToken(providedToken)) {
            return unauthorized("Invalid refresh token");
        }

        if (!"refresh".equals(jwtUtil.extractTokenType(providedToken))) {
            return badRequest("Unsupported token type");
        }

        String email = jwtUtil.extractUsername(providedToken);
        Optional<User> maybeUser = userRepository.findByEmail(email);
        if (maybeUser.isEmpty()) {
            return unauthorized("User not found");
        }

        User user = maybeUser.get();
        LocalDateTime now = LocalDateTime.now();

        // Validate stored refresh session
        if (user.getRefreshSessionId() == null ||
                user.getRefreshExpiresAt() == null ||
                user.getRefreshExpiresAt().isBefore(now) ||
                !user.getRefreshSessionId().equals(jwtUtil.extractTokenId(providedToken))) {

            clearSessions(user);
            userRepository.save(user);
            return unauthorized("Refresh session expired. Please sign in again.");
        }

        Map<String, Object> claims = buildClaims(user);

        TokenDetails newAccess = jwtUtil.generateAccessToken(user.getEmail(), claims);
        TokenDetails newRefresh = jwtUtil.generateRefreshToken(user.getEmail(), claims);

        // Rotate session identifiers
        user.setActiveSessionId(newAccess.tokenId());
        user.setSessionExpiresAt(newAccess.expiresAt().toLocalDateTime());
        user.setRefreshSessionId(newRefresh.tokenId());
        user.setRefreshExpiresAt(newRefresh.expiresAt().toLocalDateTime());

        userRepository.save(user);

        return ResponseEntity.ok(buildCompleteUserToken(user, newAccess, newRefresh));
    }

    @SuppressWarnings("null")
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @RequestHeader(value = "Authorization", required = false) String authorization) {

        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return ResponseEntity.noContent().build();
        }

        String token = authorization.substring(7);

        if (!jwtUtil.validateToken(token)) {
            return ResponseEntity.noContent().build();
        }

        String email = jwtUtil.extractUsername(token);
        userRepository.findByEmail(email).ifPresent(user -> {
            clearSessions(user);
            userRepository.save(user);
        });

        return ResponseEntity.noContent().build();
    }

    // ────────────────────────────────────────────────
    // Helper methods
    // ────────────────────────────────────────────────

    private Map<String, Object> buildClaims(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("name", user.getName());
        claims.put("role", user.getRole());
        claims.put("displayRole", user.getDisplayRole());
        claims.put("districtId", user.getDistrict().getId());

        // ✅ Add district code to claims
        claims.put("districtCode", user.getDistrict().getCode());

        // ✅ Add user ID to claims
        claims.put("userId", user.getId());

        if (user.getBranch() != null) {
            claims.put("branchId", user.getBranch().getId());
            claims.put("branchName", user.getBranch().getName());
        }
        return claims;
    }

    private UserToken buildCompleteUserToken(User user, TokenDetails access, TokenDetails refresh) {
        String accessExpires = access.expiresAt().toOffsetDateTime().toString();
        String refreshExpires = refresh.expiresAt().toOffsetDateTime().toString();

        // Build complete UserToken with all static data
        UserToken token = new UserToken();
        token.setToken(access.token());
        token.setRefreshToken(refresh.token());
        token.setTokenExpiresAt(accessExpires);
        token.setRefreshExpiresAt(refreshExpires);
        token.setId(user.getId());
        token.setName(user.getName());
        token.setEmail(user.getEmail());
        token.setRole(user.getRole());
        token.setDisplayRole(user.getDisplayRole());
        token.setIsFirstTime(user.isFirstTime());

        // District information
        if (user.getDistrict() != null) {
            token.setDistrictId(user.getDistrict().getId());
            token.setDistrictName(user.getDistrict().getName());
            token.setDistrictCode(user.getDistrict().getCode()); // ✅ Critical for frontend
        }

        // Branch information
        if (user.getBranch() != null) {
            token.setBranchId(user.getBranch().getId());
            token.setBranchName(user.getBranch().getName());
        }

        // Permissions based on role
        token.setPermissions(getPermissionsForRole(user.getRole()));

        return token;
    }

    private List<String> getPermissionsForRole(String role) {
        List<String> permissions = new ArrayList<>();

        if ("ROLE_ADMIN".equals(role)) {
            permissions.add("ADMIN");
            permissions.add("WRITER");
            permissions.add("VIEW_ALL");
            permissions.add("MANAGE_USERS");
            permissions.add("MANAGE_DISTRICTS");
            permissions.add("MANAGE_BRANCHES");
        } else if ("ROLE_SECRETARY".equals(role) || "ROLE_WRITER".equals(role)) {
            permissions.add("WRITER");
            permissions.add("VIEW_OWN");
            permissions.add("CREATE_LETTERS");
            permissions.add("VOID_LETTERS");
            permissions.add("VIEW_LETTERS");
        }

        return permissions;
    }

    private void clearSessions(User user) {
        user.setActiveSessionId(null);
        user.setSessionExpiresAt(null);
        user.setRefreshSessionId(null);
        user.setRefreshExpiresAt(null);
    }

    // Small helper methods for consistent error responses
    private ResponseEntity<Map<String, String>> unauthorized(String message) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("message", message));
    }

    private ResponseEntity<Map<String, String>> forbidden(String message) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("message", message));
    }

    private ResponseEntity<Map<String, String>> badRequest(String message) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("message", message));
    }


    @Operation(summary = "LDAP login", description = "Delegates login to external auth-service LDAP endpoint")
    @PostMapping("/ldap-login")
    public ResponseEntity<ApiResponse<LdapLoginResponse>> ldapLogin(@Valid @RequestBody LdapLoginRequest request) {
        return ok("Login successful", authService.ldapLogin(request));
    }

    @Operation(summary = "LDAP refresh", description = "Refreshes the LDAP token by delegating to auth-service")
    @PostMapping("/ldap-refresh")
    public ResponseEntity<ApiResponse<LdapLoginResponse>> ldapRefresh(@Valid @RequestBody RefreshTokenRequest request) {
        return ok("Token refreshed successfully", authService.ldapRefresh(request));
    }

}
