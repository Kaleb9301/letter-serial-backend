package com.bankofabyssinia.letter_serial_backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
// import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private String id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String role; // ROLE_SECRETARY (Writer), ROLE_ADMIN (Admin) - CONTROLS PRIVILEGES

    @Column(name = "display_role")
    private String displayRole;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "district_id", nullable = false) // Changed from office_id
    @JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
    private District district; // Changed from Office

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
    @JoinColumn(name = "branch_id", nullable = false) // Changed from department_id
    private Branch branch; // Changed from Department

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    @Column(name = "is_first_time", nullable = false)
    private boolean isFirstTime = false;

    @Column(name = "password_reset_session_id")
    private String passwordResetSessionId;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "active_session_id")
    private String activeSessionId;

    @Column(name = "session_expires_at")
    private LocalDateTime sessionExpiresAt;

    @Column(name = "refresh_session_id")
    private String refreshSessionId;

    @Column(name = "refresh_expires_at")
    private LocalDateTime refreshExpiresAt;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;
    @Column(name = "password_reset_expires_at")
    private LocalDateTime passwordResetExpiresAt;

    // getters + setters
    public LocalDateTime getPasswordResetExpiresAt() {
        return passwordResetExpiresAt;
    }

    public void setPasswordResetExpiresAt(LocalDateTime passwordResetExpiresAt) {
        this.passwordResetExpiresAt = passwordResetExpiresAt;
    }

    public User() {
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getDisplayRole() {
        return displayRole;
    }

    public void setDisplayRole(String displayRole) {
        this.displayRole = displayRole;
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

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getActiveSessionId() {
        return activeSessionId;
    }

    public void setActiveSessionId(String activeSessionId) {
        this.activeSessionId = activeSessionId;
    }

    public LocalDateTime getSessionExpiresAt() {
        return sessionExpiresAt;
    }

    public void setSessionExpiresAt(LocalDateTime sessionExpiresAt) {
        this.sessionExpiresAt = sessionExpiresAt;
    }

    public String getRefreshSessionId() {
        return refreshSessionId;
    }

    public void setRefreshSessionId(String refreshSessionId) {
        this.refreshSessionId = refreshSessionId;
    }

    public LocalDateTime getRefreshExpiresAt() {
        return refreshExpiresAt;
    }

    public void setRefreshExpiresAt(LocalDateTime refreshExpiresAt) {
        this.refreshExpiresAt = refreshExpiresAt;
    }

    public LocalDateTime getLastLoginAt() {
        return lastLoginAt;
    }

    public void setLastLoginAt(LocalDateTime lastLoginAt) {
        this.lastLoginAt = lastLoginAt;
    }

    public boolean isFirstTime() {
        return isFirstTime;
    }

    public void setFirstTime(boolean firstTime) {
        isFirstTime = firstTime;
    }

    public String getPasswordResetSessionId() {
        return passwordResetSessionId;
    }

    public void setPasswordResetSessionId(String passwordResetSessionId) {
        this.passwordResetSessionId = passwordResetSessionId;
    }
}