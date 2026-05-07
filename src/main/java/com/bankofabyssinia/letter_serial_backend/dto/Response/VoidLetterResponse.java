package com.bankofabyssinia.letter_serial_backend.dto.Response;

import java.time.LocalDateTime;

import com.bankofabyssinia.letter_serial_backend.entity.Letter;

public class VoidLetterResponse {
    private Long id;
    private Long serialNumber;
    private String writer;
    private String recipient;
    private String subject;
    private LocalDateTime letterDate;
    private LocalDateTime voidedAt;
    private String voidedBy;
    private String voidReason;
    private String districtName;
    private String status;
    private String referenceNumber;

    // Constructor from Letter entity
    public VoidLetterResponse(Letter letter) {
        this.id = letter.getId();
        this.serialNumber = letter.getSerialNumberValue();
        this.writer = letter.getWriter();
        this.recipient = letter.getRecipient();
        this.subject = letter.getSubject();
        this.letterDate = letter.getLetterDate();
        this.voidedAt = letter.getVoidedAt();
        this.voidedBy = letter.getVoidedBy();
        this.voidReason = letter.getVoidReason();
        this.districtName = letter.getDistrict() != null ? letter.getDistrict().getName() : null;
        this.status = letter.getSerialNumber() != null ? letter.getSerialNumber().getStatus().name() : null;
        this.referenceNumber = letter.getReferenceNumber();
    }

    // Getters and setters
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

    public LocalDateTime getVoidedAt() {
        return voidedAt;
    }

    public void setVoidedAt(LocalDateTime voidedAt) {
        this.voidedAt = voidedAt;
    }

    public String getVoidedBy() {
        return voidedBy;
    }

    public void setVoidedBy(String voidedBy) {
        this.voidedBy = voidedBy;
    }

    public String getVoidReason() {
        return voidReason;
    }

    public void setVoidReason(String voidReason) {
        this.voidReason = voidReason;
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

    public String getReferenceNumber() {
        return referenceNumber;
    }

    public void setReferenceNumber(String referenceNumber) {
        this.referenceNumber = referenceNumber;
    }
}