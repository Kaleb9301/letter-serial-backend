package com.bankofabyssinia.letter_serial_backend.dto.Response;

import java.time.LocalDateTime;

public class LetterWithStatusDTO {
    private Long id;
    private Long serialNumber;
    private String serialNumberDisplay;
    private String writer;
    private String recipient;
    private String subject;
    private LocalDateTime letterDate;
    private LocalDateTime createdAt;
    private String districtId;
    private String districtName;
    private String status;

    // Default constructor
    public LetterWithStatusDTO() {
    }

    // Fixed constructor with proper parameter types
    public LetterWithStatusDTO(Long id, Long serialNumber, String serialNumberDisplay, String writer, String recipient,
            String subject, LocalDateTime letterDate, LocalDateTime createdAt,
            String districtId, String districtName, String status) {
        this.id = id;
        this.serialNumber = serialNumber;
        this.serialNumberDisplay = serialNumberDisplay;
        this.writer = writer;
        this.recipient = recipient;
        this.subject = subject;
        this.letterDate = letterDate;
        this.createdAt = createdAt;
        this.districtId = districtId;
        this.districtName = districtName;
        this.status = status;
    }

    // Add getter and setter
    public String getSerialNumberDisplay() {
        return serialNumberDisplay;
    }

    public void setSerialNumberDisplay(String serialNumberDisplay) {
        this.serialNumberDisplay = serialNumberDisplay;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(Long serialNumber) {
        this.serialNumber = serialNumber;
    }

    public String getWriter() {
        return writer;
    }

    public void setWriter(String writer) {
        this.writer = writer;
    }

    public String getRecipient() {
        return recipient;
    }

    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public LocalDateTime getLetterDate() {
        return letterDate;
    }

    public void setLetterDate(LocalDateTime letterDate) {
        this.letterDate = letterDate;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getDistrictId() {
        return districtId;
    }

    public void setDistrictId(String districtId) {
        this.districtId = districtId;
    }

    public String getDistrictName() {
        return districtName;
    }

    public void setDistrictName(String districtName) {
        this.districtName = districtName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}