package com.bankofabyssinia.letter_serial_backend.service.impl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import com.bankofabyssinia.letter_serial_backend.dto.Request.LdapLoginRequest;
import com.bankofabyssinia.letter_serial_backend.dto.Request.LogOutDto;
import com.bankofabyssinia.letter_serial_backend.dto.Request.RefreshTokenRequest;
import com.bankofabyssinia.letter_serial_backend.dto.Response.LdapLoginResponse;
import com.bankofabyssinia.letter_serial_backend.dto.Response.LogOutResponse;
import com.bankofabyssinia.letter_serial_backend.exception.ExternalServiceException;
import com.bankofabyssinia.letter_serial_backend.service.AuthService;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final RestClient restClient;

    @Value("${app.auth.ldap.enabled:true}")
    private boolean ldapEnabled;

    @Value("${app.auth.ldap.url:}/ldap-login")
    private String ldapLoginUrl;

    @Value("${app.auth.ldap.url:}/refresh-token-LDAP")
    private String ldapRefreshUrl;

    @Value("${app.auth.ldap.url:}/logout")
    private String logoutUrl;


    public AuthServiceImpl(
            @Value("${app.auth.ldap.connect-timeout-ms:5000}") int connectTimeoutMs,
            @Value("${app.auth.ldap.read-timeout-ms:15000}") int readTimeoutMs
    ) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(connectTimeoutMs);
        requestFactory.setReadTimeout(readTimeoutMs);
        this.restClient = RestClient.builder().requestFactory(requestFactory).build();
    }

    
    @Override
    public LdapLoginResponse ldapLogin(LdapLoginRequest request) {
        if (!ldapEnabled) {
            throw new IllegalStateException("LDAP authentication integration is disabled");
        }
        if (!StringUtils.hasText(ldapLoginUrl)) {
            throw new IllegalStateException("app.auth.ldap.url is not configured");
        }

        try {
            LdapLoginResponse response = restClient.post()
                    .uri(ldapLoginUrl)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(LdapLoginResponse.class);

            if (response == null) {
                throw new IllegalStateException("Authentication service returned empty response");
            }
            return response;
        } catch (RestClientResponseException ex) {
            int raw = ex.getStatusCode() != null ? ex.getStatusCode().value() : -1;
            HttpStatus status = HttpStatus.resolve(raw);
            if (status == null) {
                status = HttpStatus.BAD_GATEWAY;
            }
            throw new ExternalServiceException("Authentication failed with status: " + raw, status, ex);
        } catch (RestClientException ex) {
            throw new ExternalServiceException("Unable to reach LDAP authentication service", HttpStatus.SERVICE_UNAVAILABLE, ex);
        }
    }

    @Override
    public LdapLoginResponse ldapRefresh(RefreshTokenRequest request) {
        log.info("Authentication service enabled: {}", ldapEnabled);
        log.info("LDAP Refresh URL: {}", ldapRefreshUrl);
        if (!ldapEnabled) {
            throw new IllegalStateException("Authentication integration is disabled");
        }
        if (!StringUtils.hasText(ldapRefreshUrl)) {
            throw new IllegalStateException("app.auth.ldap.url is not configured");
        }

        try {
            LdapLoginResponse response = restClient.post()
                    .uri(ldapRefreshUrl)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(LdapLoginResponse.class);

            if (response == null) {
                throw new IllegalStateException("Authentication service returned empty response");
            }
            return response;
        } catch (RestClientResponseException ex) {
            int raw = ex.getStatusCode() != null ? ex.getStatusCode().value() : -1;
            HttpStatus status = HttpStatus.resolve(raw);
            if (status == null) {
                status = HttpStatus.BAD_GATEWAY;
            }
            throw new ExternalServiceException("Authentication failed with status: " + raw, status, ex);
        } catch (RestClientException ex) {
            throw new ExternalServiceException("Unable to reach authentication service", HttpStatus.SERVICE_UNAVAILABLE, ex);
        }
    }

    @Override
    public LogOutResponse logout(LogOutDto request) {
        // Implement logout logic if needed, or delegate to auth-service if it has a logout endpoint
        log.info("Authentication service enabled: {}", ldapEnabled);
        log.info("Logout URL: {}", logoutUrl);
        if (!ldapEnabled) {
            throw new IllegalStateException("Authentication integration is disabled");
        }
        if (!StringUtils.hasText(ldapRefreshUrl)) {
            throw new IllegalStateException("app.auth.ldap.url is not configured");
        }

        try {
            LogOutResponse response = restClient.post()
                    .uri(logoutUrl)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(LogOutResponse.class);

            if (response == null) {
                throw new IllegalStateException("LDAP service returned empty response");
            }
            return response;
        } catch (RestClientResponseException ex) {
            int raw = ex.getStatusCode() != null ? ex.getStatusCode().value() : -1;
            HttpStatus status = HttpStatus.resolve(raw);
            if (status == null) {
                status = HttpStatus.BAD_GATEWAY;
            }
            throw new ExternalServiceException("Logout  failed with status: " + raw, status, ex);
        } catch (RestClientException ex) {
            throw new ExternalServiceException("Unable to reach authentication service", HttpStatus.SERVICE_UNAVAILABLE, ex);
        }
    }

}
