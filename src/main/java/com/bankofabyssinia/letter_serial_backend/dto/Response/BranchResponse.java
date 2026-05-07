package com.bankofabyssinia.letter_serial_backend.dto.Response;

import com.bankofabyssinia.letter_serial_backend.entity.Branch;

public class BranchResponse {
    private String id;
    private String name;
    private String prefix;
    private boolean isDefault;
    private String districtId;

    public BranchResponse() {
    }

    public BranchResponse(Branch branch) {
        this.id = branch.getId();
        this.name = branch.getName();
        this.prefix = branch.getPrefix();
        this.isDefault = branch.isDefault();
        this.districtId = branch.getDistrict() != null ? branch.getDistrict().getId() : null;
    }

    // Getters and setters
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

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public boolean isDefault() {
        return isDefault;
    }

    public void setDefault(boolean isDefault) {
        this.isDefault = isDefault;
    }

    public String getDistrictId() {
        return districtId;
    }

    public void setDistrictId(String districtId) {
        this.districtId = districtId;
    }
}