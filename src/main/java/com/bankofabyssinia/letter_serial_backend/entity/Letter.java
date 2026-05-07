package com.bankofabyssinia.letter_serial_backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "letters", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "district_id", "reference_number" }) // Changed from office_id
})
public class Letter {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "serial_number_id", nullable = false)
    @JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
    private SerialNumber serialNumber;

    @Column(nullable = false)
    private String writer;

    @Column(nullable = false)
    private String recipient;

    @Column(nullable = false)
    private String subject;

    @Column(name = "letter_date", nullable = false)
    private LocalDateTime letterDate;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "reference_number", nullable = false, length = 200)
    private String referenceNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "district_id", nullable = false) // Changed from office_id
    @JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
    private District district; // Changed from Office

    // Store branch info at creation time for guaranteed lookup later
    @Column(name = "branch_id") // Changed from department_id
    private String branchId; // Changed from departmentId

    @Column(name = "branch_name") // Changed from department_name
    private String branchName; // Changed from departmentName

    // Void status fields
    @Column(name = "is_voided", nullable = false)
    private Boolean isVoided = false;

    @Column(name = "void_reason")
    private String voidReason;

    @Column(name = "voided_at")
    private LocalDateTime voidedAt;

    @Column(name = "voided_by")
    private String voidedBy;

    public Letter() {
    }

    // Fixed constructor - use LocalDateTime for letterDate
    public Letter(SerialNumber serialNumber, String writer, String recipient, String subject,
            LocalDateTime letterDate, District district) { // Changed from Office
        this.serialNumber = serialNumber;
        this.writer = writer;
        this.recipient = recipient;
        this.subject = subject;
        this.letterDate = letterDate;
        this.district = district; // Changed from office
        this.createdAt = LocalDateTime.now();
        this.isVoided = false; // Default to not voided
    }

    // branch fields can be set after construction if available
    public String getReferenceNumber() {
        return referenceNumber;
    }

    public void setReferenceNumber(String referenceNumber) {
        this.referenceNumber = referenceNumber;
    }

    public String getBranchId() { // Changed from getDepartmentId
        return branchId;
    }

    public void setBranchId(String branchId) { // Changed from setDepartmentId
        this.branchId = branchId;
    }

    public String getBranchName() { // Changed from getDepartmentName
        return branchName;
    }

    public void setBranchName(String branchName) { // Changed from setDepartmentName
        this.branchName = branchName;
    }

    // Void status getters and setters
    public Boolean getIsVoided() {
        return isVoided;
    }

    public void setIsVoided(Boolean isVoided) {
        this.isVoided = isVoided;
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

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public SerialNumber getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(SerialNumber serialNumber) {
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

    public District getDistrict() { // Changed from getOffice
        return district;
    }

    public void setDistrict(District district) { // Changed from setOffice
        this.district = district;
    }

    public String getSerialNumberDisplay() {
        return serialNumber != null ? serialNumber.getSerialDisplay() : null;
    }

    public Long getSerialNumberValue() {
        return serialNumber != null ? serialNumber.getSerialNumber() : null;
    }
}