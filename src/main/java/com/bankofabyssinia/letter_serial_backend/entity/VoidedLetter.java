package com.bankofabyssinia.letter_serial_backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "voided_letters")
public class VoidedLetter {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "original_letter_id", nullable = false)
    private Long originalLetterId;

    @Column(name = "serial_number", nullable = false)
    private Long serialNumber;

    @Column(name = "display_serial", nullable = false)
    private String displaySerial;

    @Column(nullable = false)
    private String writer;

    @Column(nullable = false)
    private String recipient;

    @Column(nullable = false)
    private String subject;

    @Column(name = "letter_date", nullable = false)
    private LocalDateTime letterDate;

    @Column(name = "original_created_at", nullable = false)
    private LocalDateTime originalCreatedAt;

    @Column(name = "voided_at", nullable = false)
    private LocalDateTime voidedAt;

    @Column(name = "voided_by", nullable = false)
    private String voidedBy;

    @Column(name = "void_reason", nullable = false)
    private String voidReason;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "district_id", nullable = false)
    private District district;

    @Column(name = "branch_id")
    private String branchId;

    @Column(name = "branch_name")
    private String branchName;

    // Constructors
    public VoidedLetter() {
    }

    public VoidedLetter(Letter letter, String voidedBy, String voidReason) {
        this.originalLetterId = letter.getId();
        this.serialNumber = letter.getSerialNumber().getSerialNumber();
        this.displaySerial = letter.getSerialNumber().getDisplaySerial();
        this.writer = letter.getWriter();
        this.recipient = letter.getRecipient();
        this.subject = letter.getSubject();
        this.letterDate = letter.getLetterDate();
        this.originalCreatedAt = letter.getCreatedAt();
        this.voidedAt = LocalDateTime.now();
        this.voidedBy = voidedBy;
        this.voidReason = voidReason;
        this.district = letter.getDistrict();
        // carry branch info from the original letter if present
        try {
            this.branchId = letter.getBranchId();
            this.branchName = letter.getBranchName();
        } catch (Exception e) {
            // ignore if not available
        }
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getOriginalLetterId() {
        return originalLetterId;
    }

    public void setOriginalLetterId(Long originalLetterId) {
        this.originalLetterId = originalLetterId;
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

    public LocalDateTime getOriginalCreatedAt() {
        return originalCreatedAt;
    }

    public void setOriginalCreatedAt(LocalDateTime originalCreatedAt) {
        this.originalCreatedAt = originalCreatedAt;
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

    public District getDistrict() {
        return district;
    }

    public void setDistrict(District district) {
        this.district = district;
    }

    public String getBranchId() {
        return branchId;
    }

    public void setBranchId(String branchId) {
        this.branchId = branchId;
    }

    public String getBranchName() {
        return branchName;
    }

    public void setBranchName(String branchName) {
        this.branchName = branchName;
    }
}