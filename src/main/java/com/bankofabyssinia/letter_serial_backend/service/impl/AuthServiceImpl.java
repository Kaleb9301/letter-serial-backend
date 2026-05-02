package com.bankofabyssinia.letter_serial_backend.service.impl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import com.bankofabyssinia.letter_serial_backend.dto.Request.LdapLoginRequest;
import com.bankofabyssinia.letter_serial_backend.dto.Response.LdapLoginResponse;
import com.bankofabyssinia.letter_serial_backend.service.AuthService;

@Service
public class AuthServiceImpl implements AuthService {

    private final RestClient restClient;

    @Value("${app.auth.ldap.enabled:true}")
    private boolean ldapEnabled;

    @Value("${app.auth.ldap.url:}")
    private String ldapLoginUrl;

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
                throw new IllegalStateException("LDAP service returned empty response");
            }
            return response;
        } catch (RestClientResponseException ex) {
            throw new IllegalArgumentException("LDAP authentication failed with status: " + ex.getStatusCode().value());
        } catch (RestClientException ex) {
            throw new IllegalStateException("Unable to reach LDAP authentication service");
        }
    }
}
