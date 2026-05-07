package com.bankofabyssinia.letter_serial_backend.dto.Response;

import java.util.ArrayList;
import java.util.List;

public class UserToken {
    private String token;
    private String refreshToken;
    private String tokenExpiresAt;
    private String refreshExpiresAt;

    // User identity
    private String id;
    private String name;
    private String email;
    private String role;
    private String displayRole;

    // District information (static data loaded once)
    private String districtId;
    private String districtName;
    private String districtCode;

    // Branch information (static data loaded once)
    private String branchId;
    private String branchName;

    // Permissions (derived from role, loaded once)
    private List<String> permissions = new ArrayList<>();

    // First time login flag
    private Boolean isFirstTime = false;

    public UserToken() {
    }

    // Constructor for basic login (backward compatible)
    public UserToken(String token, String refreshToken, String tokenExpiresAt, String refreshExpiresAt,
            String name, String role, String displayRole,
            String districtId, String districtName) {
        this.token = token;
        this.refreshToken = refreshToken;
        this.tokenExpiresAt = tokenExpiresAt;
        this.refreshExpiresAt = refreshExpiresAt;
        this.name = name;
        this.role = role;
        this.displayRole = displayRole;
        this.districtId = districtId;
        this.districtName = districtName;
    }

    // Complete constructor with all data
    public UserToken(String token, String refreshToken, String tokenExpiresAt, String refreshExpiresAt,
            String id, String name, String email, String role, String displayRole,
            String districtId, String districtName, String districtCode,
            String branchId, String branchName, List<String> permissions, Boolean isFirstTime) {
        this.token = token;
        this.refreshToken = refreshToken;
        this.tokenExpiresAt = tokenExpiresAt;
        this.refreshExpiresAt = refreshExpiresAt;
        this.id = id;
        this.name = name;
        this.email = email;
        this.role = role;
        this.displayRole = displayRole;
        this.districtId = districtId;
        this.districtName = districtName;
        this.districtCode = districtCode;
        this.branchId = branchId;
        this.branchName = branchName;
        this.permissions = permissions != null ? permissions : new ArrayList<>();
        this.isFirstTime = isFirstTime != null ? isFirstTime : false;
    }

    // Getters and Setters for all fields

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getTokenExpiresAt() {
        return tokenExpiresAt;
    }

    public void setTokenExpiresAt(String tokenExpiresAt) {
        this.tokenExpiresAt = tokenExpiresAt;
    }

    public String getRefreshExpiresAt() {
        return refreshExpiresAt;
    }

    public void setRefreshExpiresAt(String refreshExpiresAt) {
        this.refreshExpiresAt = refreshExpiresAt;
    }

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

    public String getDistrictCode() {
        return districtCode;
    }

    public void setDistrictCode(String districtCode) {
        this.districtCode = districtCode;
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

    public List<String> getPermissions() {
        return permissions != null ? permissions : new ArrayList<>();
    }

    public void setPermissions(List<String> permissions) {
        this.permissions = permissions != null ? permissions : new ArrayList<>();
    }

    public Boolean getIsFirstTime() {
        return isFirstTime;
    }

    public void setIsFirstTime(Boolean isFirstTime) {
        this.isFirstTime = isFirstTime;
    }
}