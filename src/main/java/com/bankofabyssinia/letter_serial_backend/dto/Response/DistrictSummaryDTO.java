 // src/main/java/com/example/letter_serial_system/dto/DistrictSummaryDTO.java
package com.bankofabyssinia.letter_serial_backend.dto.Response;

import com.bankofabyssinia.letter_serial_backend.entity.District;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class DistrictSummaryDTO {
    private String id;
    private String name;
    private String code;
    private String description;
    private Long branchCount;
    private Boolean active;

    public DistrictSummaryDTO() {
    }

    public DistrictSummaryDTO(District district) {
        this.id = district.getId();
        this.name = district.getName();
        this.code = district.getCode();
        this.description = district.getDescription();
        this.active = district.isActive();
        this.branchCount = (long) district.getBranches().size();
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

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getBranchCount() {
        return branchCount;
    }

    public void setBranchCount(Long branchCount) {
        this.branchCount = branchCount;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }
}