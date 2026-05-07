package com.bankofabyssinia.letter_serial_backend.entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
// import java.util.UUID;

@Entity
@Table(name = "districts")
public class District {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private String id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String code;

    private String description;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    // Change cascade type to PERSIST and MERGE only
    @OneToMany(mappedBy = "district", fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
    private List<Branch> branches = new ArrayList<>();

    public District() {
    }

    public District(String name, String code, String description, boolean isActive) {
        this.name = name;
        this.code = code;
        this.description = description;
        this.isActive = isActive;
    }

    // Simplified helper method
    public void addBranch(Branch branch) {
        if (branch != null) {
            branches.add(branch);
            branch.setDistrict(this);
        }
    }

    // Simplified helper method
    public void removeBranch(Branch branch) {
        if (branch != null) {
            branches.remove(branch);
            branch.setDistrict(null);
        }
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

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public List<Branch> getBranches() {
        return branches;
    }

    public void setBranches(List<Branch> branches) {
        this.branches = branches;
    }
}