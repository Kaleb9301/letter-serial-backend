package com.bankofabyssinia.letter_serial_backend.dto.Response;

import java.time.LocalDateTime;
import com.bankofabyssinia.letter_serial_backend.entity.Letter;
import com.bankofabyssinia.letter_serial_backend.entity.VoidedLetter;

public class CombinedLetterDTO {
    private String type; // "ACTIVE" or "VOIDED"
    private Long id;
    private Long serialNumber;
    private String displaySerial;
    private String referenceNumber;
    private String writer;
    private String recipient;
    private String subject;
    private LocalDateTime letterDate;
    private LocalDateTime createdAt;
    private String districtId;
    private String districtName;
    private String status;
    private String voidReason;
    private LocalDateTime voidedAt;
    private String voidedBy;
    private String branchName;

    // Constructors for Active Letter
    public CombinedLetterDTO(Letter letter) {
        this.type = letter.getIsVoided() ? "VOIDED" : "ACTIVE";
        this.id = letter.getId();
        this.serialNumber = letter.getSerialNumberValue();
        this.displaySerial = letter.getSerialNumberDisplay();
        this.referenceNumber = letter.getReferenceNumber();
        this.writer = letter.getWriter();
        this.recipient = letter.getRecipient();
        this.subject = letter.getSubject();
        this.letterDate = letter.getLetterDate();
        this.createdAt = letter.getCreatedAt();
        this.districtId = letter.getDistrict().getId();
        this.districtName = letter.getDistrict().getName();

        // Set status based on void status and serial status
        if (letter.getIsVoided()) {
            this.status = "VOIDED";
        } else {
            this.status = letter.getSerialNumber().getStatus().name();
        }

        this.voidReason = letter.getVoidReason();
        this.voidedAt = letter.getVoidedAt();
        this.voidedBy = letter.getVoidedBy();
        this.branchName = letter.getBranchName();
    }

    // Constructors for Voided Letter
    public CombinedLetterDTO(VoidedLetter voidedLetter) {
        this.type = "VOIDED";
        this.id = voidedLetter.getId();
        this.serialNumber = voidedLetter.getSerialNumber();
        this.displaySerial = voidedLetter.getDisplaySerial();
        this.writer = voidedLetter.getWriter();
        this.recipient = voidedLetter.getRecipient();
        this.subject = voidedLetter.getSubject();
        this.letterDate = voidedLetter.getLetterDate();
        this.createdAt = voidedLetter.getOriginalCreatedAt();
        this.districtId = voidedLetter.getDistrict().getId();
        this.districtName = voidedLetter.getDistrict().getName();
        this.status = "VOIDED";
        this.voidReason = voidedLetter.getVoidReason();
        this.voidedAt = voidedLetter.getVoidedAt();
        this.voidedBy = voidedLetter.getVoidedBy();
        // populate branch name if available on the voided record
        this.branchName = voidedLetter.getBranchName();
    }

    // Getters and Setters
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

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

    public String getDisplaySerial() {
        return displaySerial;
    }

    public void setDisplaySerial(String displaySerial) {
        this.displaySerial = displaySerial;
    }

    public String getReferenceNumber() {
        return referenceNumber;
    }

    public void setReferenceNumber(String referenceNumber) {
        this.referenceNumber = referenceNumber;
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

    public String getVoidReason() {
        return voidReason;
    }

    public void setVoidReason(String voidReason) {
        this.voidReason = voidReason;
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

    public String getBranchName() {
        return branchName;
    }

    public void setBranchName(String branchName) {
        this.branchName = branchName;
    }
}