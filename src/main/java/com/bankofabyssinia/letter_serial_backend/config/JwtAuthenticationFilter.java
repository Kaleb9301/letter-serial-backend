package com.bankofabyssinia.letter_serial_backend.config;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import com.bankofabyssinia.letter_serial_backend.exception.JwtAuthenticationException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;





@Component
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    // private final JwtUtil jwtUtil;
    // private final UserRepository userRepository;

    

    // public JwtAuthenticationFilter(JwtUtil jwtUtil, UserRepository userRepository) {
    //     this.jwtUtil = jwtUtil;
    //     this.userRepository = userRepository;
    // }

    // private final RestClient restClient;

    private final HandlerExceptionResolver handlerExceptionResolver;

    @Value("${app.auth.ldap.enabled:true}")
    private boolean ldapEnabled;

    @Value("${app.auth.ldap.url:}/validate-token")
    private String validationUrl;

    public JwtAuthenticationFilter(
            @Qualifier("handlerExceptionResolver") HandlerExceptionResolver handlerExceptionResolver,
            @Value("${app.auth.ldap.connect-timeout-ms:5000}") int connectTimeoutMs,
            @Value("${app.auth.ldap.read-timeout-ms:15000}") int readTimeoutMs
    ) {
        this.handlerExceptionResolver = handlerExceptionResolver;
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(connectTimeoutMs);
        requestFactory.setReadTimeout(readTimeoutMs);
        // this.restTemplate = new RestTemplate(requestFactory);
    }

    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    private static final List<String> PUBLIC_ENDPOINTS = Arrays.asList(
            "/swagger",
            "/swagger/",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/v3/api-docs/**",
            "/webjars/**",
            "/actuator/**",
            "/error",

            // Auth endpoints
            "/api/auth/**",
            "/auth-service/api/auth/**",
           "/auth/**",
            "/api/public/**",
            "/api/orale/employeesListByUUID/**"
    );

    // @Override
    // protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
    //     throws ServletException, IOException {
    //     String header = request.getHeader("Authorization");
    //     if (header != null && header.startsWith("Bearer ")) {
    //         String token = header.substring(7);
    //         if (jwtUtil.validateToken(token) && "access".equals(jwtUtil.extractTokenType(token))) {
    //             Claims claims = jwtUtil.extractAllClaims(token);
    //             String userEmail = claims.getSubject();
    //             Optional<User> maybeUser = userRepository.findByEmail(userEmail);
    //             if (maybeUser.isPresent()) {
    //                 User user = maybeUser.get();
    //                 if (user.getActiveSessionId() != null
    //                         && user.getSessionExpiresAt() != null
    //                         && user.getSessionExpiresAt().isAfter(java.time.LocalDateTime.now())
    //                         && user.getActiveSessionId().equals(claims.getId())) {
    //                     List<GrantedAuthority> authorities = jwtUtil.getAuthorities(claims);

    //                     UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
    //                             claims, null, authorities);
    //                     authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
    //                     SecurityContextHolder.getContext().setAuthentication(authToken);
    //                 }
    //             }
    //         }
    //     }
    //     filterChain.doFilter(request, response);
    // }
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        log.info("LDAP Authentication enabled: {}", ldapEnabled);
        log.info("LDAP Login URL: {}", validationUrl);
        if (!ldapEnabled) {
            resolveJwtAuthenticationException(request, response,
                    new JwtAuthenticationException("LDAP authentication integration is disabled"));
            return;
        }
        if (!StringUtils.hasText(validationUrl)) {
            resolveJwtAuthenticationException(request, response,
                    new JwtAuthenticationException("app.auth.ldap.url is not configured"));
            return;
        }

        String path = request.getServletPath(); // ✅ IMPORTANT FIX FOR WAR

        log.info("Incoming request: {}", path);

        boolean isPublic = PUBLIC_ENDPOINTS.stream()
                .anyMatch(pattern -> pathMatcher.match(pattern, path));

        if (isPublic) {
            filterChain.doFilter(request, response);
            return;
        }

        String jwt = parseJwt(request);
        if (jwt == null) {
            resolveJwtAuthenticationException(request, response,
                    new JwtAuthenticationException("Missing Authorization header"));
            return;
        }

        if (!validateToken(jwt, request)) {
            resolveJwtAuthenticationException(request, response,
                    new JwtAuthenticationException("Invalid or expired token"));
            return;
        }

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                "validated-user",
                null,
                Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        try {
            filterChain.doFilter(request, response);
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    private void resolveJwtAuthenticationException(
            HttpServletRequest request,
            HttpServletResponse response,
            JwtAuthenticationException exception) throws IOException, ServletException {
        handlerExceptionResolver.resolveException(request, response, null, exception);
    }
    private String parseJwt(HttpServletRequest request) {
        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }
        
    public boolean validateToken(String token, HttpServletRequest request) {
        try {

            // String VALIDATION_URL = "http://boainhousesys.bankofabyssinia.com:8085/auth-service/api/auth/validate-token";
            // // String VALIDATION_URL = "http://localhost:8085/auth-service/api/auth/validate-token";

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + token);
            headers.setContentType(MediaType.APPLICATION_JSON);

            String requestJson = "{\"accessToken\":\"" + token + "\"}";

            String ipAddress = request.getRemoteAddr();
            String userAgent = request.getHeader("User-Agent");

            if ("0:0:0:0:0:0:0:1".equals(ipAddress)) {
                ipAddress = "127.0.0.1";
            }

            headers.set("remote-address", ipAddress);
            headers.set("User-Agent", userAgent);
            headers.set("X-Fingerprint", ipAddress + "|" + userAgent);

            HttpEntity<String> entity = new HttpEntity<>(requestJson, headers);

            RestTemplate restTemplate = new RestTemplate();

            ResponseEntity<String> response = restTemplate.exchange(
                    validationUrl,
                    HttpMethod.GET,
                    entity,
                    String.class
            );

            log.info("Token validation status: {}", response.getStatusCode());

           

            return response.getBody() != null && response.getBody().contains("\"isValid\":true");

        } catch (RestClientException e) {
            log.error("Token validation error: {}", e.getMessage());
            return false;
        }
    }
}


