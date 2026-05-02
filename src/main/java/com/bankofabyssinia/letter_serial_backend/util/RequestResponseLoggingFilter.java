package com.bankofabyssinia.letter_serial_backend.util;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
public class RequestResponseLoggingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RequestResponseLoggingFilter.class);

    private static final Set<String> SENSITIVE_KEYS = new HashSet<>(Arrays.asList(
            "password", "pass", "token", "secret", "authorization", "api-key", "apikey"));

    @Value("${app.request-logging.enabled:true}")
    private boolean enabled;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        if (!enabled) {
            filterChain.doFilter(request, response);
            return;
        }

        long start = System.currentTimeMillis();
        String uriWithQuery = buildUriWithMaskedQuery(request);

        log.info("Incoming request: {} {}", request.getMethod(), uriWithQuery);
        try {
            filterChain.doFilter(request, response);
        } finally {
            long durationMs = System.currentTimeMillis() - start;
            log.info("Outgoing response: {} {} -> status={} durationMs={}",
                    request.getMethod(),
                    uriWithQuery,
                    response.getStatus(),
                    durationMs);
        }
    }

    private String buildUriWithMaskedQuery(HttpServletRequest request) {
        String query = request.getQueryString();
        if (query == null || query.isBlank()) {
            return request.getRequestURI();
        }

        String maskedQuery = Arrays.stream(query.split("&"))
                .map(this::maskQueryParam)
                .collect(Collectors.joining("&"));

        return request.getRequestURI() + "?" + maskedQuery;
    }

    private String maskQueryParam(String pair) {
        int idx = pair.indexOf('=');
        if (idx < 0) {
            return pair;
        }

        String key = pair.substring(0, idx);
        String value = pair.substring(idx + 1);
        String normalizedKey = key.toLowerCase(Locale.ROOT);

        if (SENSITIVE_KEYS.stream().anyMatch(normalizedKey::contains)) {
            return key + "=***";
        }

        return key + "=" + value;
    }
}
