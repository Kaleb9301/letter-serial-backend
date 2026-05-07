package com.bankofabyssinia.letter_serial_backend.service;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtUtil {
    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long expiration;

    @Value("${jwt.refresh-expiration}") // Changed to match properties kebab-case (Spring converts automatically)
    private Long refreshExpiration; // Removed hardcoded default to rely on properties

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    public TokenDetails generateAccessToken(String subject, Map<String, Object> additionalClaims) {
        return generateToken(subject, additionalClaims, expiration, "access");
    }

    public TokenDetails generateRefreshToken(String subject, Map<String, Object> additionalClaims) {
        return generateToken(subject, additionalClaims, refreshExpiration, "refresh");
    }

    private TokenDetails generateToken(String subject, Map<String, Object> additionalClaims, Long ttl, String type) {
        Map<String, Object> claims = new HashMap<>(additionalClaims);
        Instant now = Instant.now();
        Instant expiry = now.plusMillis(ttl);
        String tokenId = UUID.randomUUID().toString();

        claims.put("type", type);

        String token = Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .id(tokenId)
                .signWith(getSigningKey())
                .compact();

        return new TokenDetails(token, tokenId, ZonedDateTime.ofInstant(expiry, ZoneId.systemDefault()));
    }

    public Boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public List<GrantedAuthority> getAuthorities(Claims claims) {
        String role = claims.get("role", String.class);
        return List.of(new SimpleGrantedAuthority(role));
    }

    public String extractTokenId(String token) {
        return extractAllClaims(token).getId();
    }

    public String extractTokenType(String token) {
        return extractAllClaims(token).get("type", String.class);
    }

    public boolean isExpired(Date expirationDate) {
        return expirationDate.before(new Date());
    }

    public record TokenDetails(String token, String tokenId, ZonedDateTime expiresAt) {
    }
}