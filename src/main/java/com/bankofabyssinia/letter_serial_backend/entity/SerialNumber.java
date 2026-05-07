package com.bankofabyssinia.letter_serial_backend.entity;

import com.bankofabyssinia.letter_serial_backend.enums.SerialNumberStatus;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "serial_numbers", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "serial_number", "district_id", "branch_id", "status" }) // Changed office_id
                                                                                                   // to district_id,
                                                                                                   // department_id to
                                                                                                   // branch_id
})
public class SerialNumber {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "serial_number", nullable = false)
    private Long serialNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SerialNumberStatus status;

    @Column(name = "created_by", nullable = false, length = 100)
    private String createdBy;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    @Column(name = "voided_timestamp")
    private LocalDateTime voidedTimestamp;

    @Column(name = "voided_sequence")
    private Integer voidedSequence;
    @Column(name = "voided_by", length = 100)
    private String voidedBy;

    @Column(name = "voided_at")
    private LocalDateTime voidedAt;

    @Column(name = "void_reason", length = 500)
    private String voidReason;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "district_id", nullable = false) // Changed from office_id
    @JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
    private District district; // Changed from Office

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id") // Changed from department_id
    @JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
    private Branch branch; // Changed from Department

    @Column(name = "display_serial", nullable = false, length = 50)
    private String displaySerial;

    public SerialNumber() {
    }

    private String generateVoidedDisplaySerial() {
        if (this.status == SerialNumberStatus.VOIDED && this.voidedSequence != null) {
            return this.serialNumber + "-VOIDED-" + this.voidedSequence;
        }
        return this.serialNumber + "-VOIDED";
    }

    // Constructor for new serials (USED status) - district only (backwards compat)
    public SerialNumber(Long serialNumber, SerialNumberStatus status, String createdBy,
            LocalDateTime createdAt, District district) { // Changed from Office
        this.serialNumber = serialNumber;
        this.displaySerial = serialNumber.toString(); // Default to regular serial
        this.status = status;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
        this.district = district; // Changed from office
    }

    // Constructor for new serials with branch
    public SerialNumber(Long serialNumber, SerialNumberStatus status, String createdBy,
            LocalDateTime createdAt, District district, Branch branch) { // Changed from Office, Department
        this.serialNumber = serialNumber;
        this.displaySerial = serialNumber.toString(); // Default to regular serial
        this.status = status;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
        this.district = district; // Changed from office
        this.branch = branch; // Changed from department
    }

    // Constructor for voided serials (district-only)
    public SerialNumber(Long serialNumber, SerialNumberStatus status, String createdBy,
            LocalDateTime createdAt, District district, String voidedBy, // Changed from Office
            LocalDateTime voidedAt, String voidReason, Integer voidedSequence) {
        this.serialNumber = serialNumber;
        this.status = status;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
        this.district = district; // Changed from office
        this.voidedBy = voidedBy;
        this.voidedAt = voidedAt;
        this.voidReason = voidReason;
        this.voidedSequence = voidedSequence;

        if (status == SerialNumberStatus.VOIDED) {
            this.displaySerial = generateVoidedDisplaySerial();
        } else {
            this.displaySerial = serialNumber.toString();
        }
    }

    // Constructor for voided serials with branch
    public SerialNumber(Long serialNumber, SerialNumberStatus status, String createdBy,
            LocalDateTime createdAt, District district, Branch branch, String voidedBy, // Changed from Office,
                                                                                        // Department
            LocalDateTime voidedAt, String voidReason, Integer voidedSequence) {
        this.serialNumber = serialNumber;
        this.status = status;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
        this.district = district; // Changed from office
        this.branch = branch; // Changed from department
        this.voidedBy = voidedBy;
        this.voidedAt = voidedAt;
        this.voidReason = voidReason;
        this.voidedSequence = voidedSequence;

        if (status == SerialNumberStatus.VOIDED) {
            this.displaySerial = generateVoidedDisplaySerial();
        } else {
            this.displaySerial = serialNumber.toString();
        }
    }

    // Helper method to get the display value
    public String getSerialDisplay() {
        return this.displaySerial;
    }

    // Helper method to check if serial is voided
    public boolean isVoided() {
        return this.status == SerialNumberStatus.VOIDED;
    }

    // Helper method to check if serial is used
    public boolean isUsed() {
        return this.status == SerialNumberStatus.USED;
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

    public SerialNumberStatus getStatus() {
        return status;
    }

    public void setStatus(SerialNumberStatus status) {
        this.status = status;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getVoidedBy() {
        return voidedBy;
    }

    public void setVoidedBy(String voidedBy) {
        this.voidedBy = voidedBy;
    }

    public LocalDateTime getVoidedAt() {
        return voidedAt;
    }

    public void setVoidedAt(LocalDateTime voidedAt) {
        this.voidedAt = voidedAt;
    }

    public String getVoidReason() {
        return voidReason;
    }

    public void setVoidReason(String voidReason) {
        this.voidReason = voidReason;
    }

    public District getDistrict() { // Changed from getOffice
        return district;
    }

    public void setDistrict(District district) { // Changed from setOffice
        this.district = district;
    }

    public Branch getBranch() { // Changed from getDepartment
        return branch;
    }

    public void setBranch(Branch branch) { // Changed from setDepartment
        this.branch = branch;
    }

    public String getDisplaySerial() {
        return displaySerial;
    }

    public void setDisplaySerial(String displaySerial) {
        this.displaySerial = displaySerial;
    }

    public LocalDateTime getVoidedTimestamp() {
        return voidedTimestamp;
    }

    public void setVoidedTimestamp(LocalDateTime voidedTimestamp) {
        this.voidedTimestamp = voidedTimestamp;
    }

    public Integer getVoidedSequence() {
        return voidedSequence;
    }

    public void setVoidedSequence(Integer voidedSequence) {
        this.voidedSequence = voidedSequence;
    }

    @Override
    public String toString() {
        return "SerialNumber{" +
                "id=" + id +
                ", serialNumber=" + serialNumber +
                ", status=" + status +
                ", displaySerial='" + displaySerial + '\'' +
                ", district=" + (district != null ? district.getId() : "null") + // Changed from office
                '}';
    }
}