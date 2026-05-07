package com.bankofabyssinia.letter_serial_backend.service;

import java.time.Duration;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class EmailService {

    private final WebClient webClient;

    @Value("${email.microservice.url}")
    private String emailMicroserviceUrl;

    @Value("${email.microservice.api-key}")
    private String emailApiKey;

    @Value("${email.microservice.timeout-ms:5000}")
    private long emailTimeoutMs;

    public EmailService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    public boolean sendEmailViaMicroservice(String to, String subject, String template, Map<String, Object> variables) {
        try {
            var requestBody = Map.of(
                    "to", to,
                    "subject", subject,
                    "template", template,
                    "variables", variables != null ? variables : Map.of());

            log.info("Forwarding email request to Email microservice: {}", emailMicroserviceUrl);

            var response = webClient.post()
                    .uri(emailMicroserviceUrl)
                    .header("X-API-KEY", emailApiKey)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .timeout(Duration.ofMillis(emailTimeoutMs))
                    .onErrorResume(e -> {
                        log.error("Email microservice exception: {}", e.getMessage());
                        return null;
                    })
                    .block();

            if (response != null && "success".equalsIgnoreCase(String.valueOf(response.get("status")))) {
                log.info("Email sent successfully via microservice to {}", to);
                return true;
            } else {
                log.warn("Email microservice returned non-success: {}", response);
            }
            return false;
        } catch (Exception e) {
            log.error("Failed contacting Email microservice: {}", e.getMessage(), e);
            return false;
        }
    }
}
