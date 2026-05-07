package com.bankofabyssinia.letter_serial_backend.entity;

// import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.EqualsAndHashCode;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
// import jakarta.persistence.GeneratedValue;
// import jakarta.persistence.GenerationType;
// import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import lombok.Data;

@Entity
@Data
@EqualsAndHashCode(callSuper = true)
@Table(name = "branches")
public class Branch extends BaseEntity<String> {
    
    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String prefix;

    @Column(name = "branch_code")
    private String code;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "is_default", nullable = false)
    private boolean isDefault = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "district_id", nullable = false)
    @JsonIgnoreProperties({ "hibernateLazyInitializer", "handler", "branches" })
    private District district;

    public Branch() {
    }

    // Simplified constructors - don't modify district
    public Branch(String name, String prefix, boolean isDefault, District district) {
        this.name = name;
        this.prefix = prefix;
        this.isDefault = isDefault;
        this.district = district;
    }

    public Branch(String name, String code, String description, String prefix, boolean isDefault, District district) {
        this.name = name;
        this.code = code;
        this.description = description;
        this.prefix = prefix;
        this.isDefault = isDefault;
        this.district = district;
    }

    // // Getters and Setters
    // public String getId() {
    //     return id;
    // }

    // public void setId(String id) {
    //     this.id = id;
    // }

    // public String getName() {
    //     return name;
    // }

    // public void setName(String name) {
    //     this.name = name;
    // }

    // public String getPrefix() {
    //     return prefix;
    // }

    // public void setPrefix(String prefix) {
    //     this.prefix = prefix;
    // }

    // public String getCode() {
    //     return code;
    // }

    // public void setCode(String code) {
    //     this.code = code;
    // }

    // public String getDescription() {
    //     return description;
    // }

    // public void setDescription(String description) {
    //     this.description = description;
    // }

    // public boolean isDefault() {
    //     return isDefault;
    // }

    // public void setDefault(boolean aDefault) {
    //     isDefault = aDefault;
    // }

    // public District getDistrict() {
    //     return district;
    // }

    // public void setDistrict(District district) {
    //     this.district = district;
    // }
}