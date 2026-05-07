package com.bankofabyssinia.letter_serial_backend.dto.Request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public class CreateLetterRequest {
    @NotBlank
    private String recipient;

    @NotBlank
    private String subject;

    @NotNull
    private LocalDate letterDate;

    // ⚠️ REMOVE districtId from here - it comes from JWT token
    // @NotBlank
    // private String districtId;

    @NotBlank
    @Size(max = 200)
    @Pattern(regexp = "^[\\p{L}\\p{N}/\\-\\s]+$", message = "Reference number can only contain letters, numbers, slashes, dashes and spaces")
    private String referenceNumber;

    // Optional branch information to be stored with the letter
    private String branchId;
    private String branchName;

    public CreateLetterRequest() {
    }

    // Getters and Setters
    public String getReferenceNumber() {
        return referenceNumber;
    }

    public void setReferenceNumber(String referenceNumber) {
        this.referenceNumber = referenceNumber;
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

    public LocalDate getLetterDate() {
        return letterDate;
    }

    public void setLetterDate(LocalDate letterDate) {
        this.letterDate = letterDate;
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