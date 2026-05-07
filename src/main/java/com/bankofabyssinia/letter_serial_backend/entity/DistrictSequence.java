package com.bankofabyssinia.letter_serial_backend.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "district_sequences") // Changed from office_sequences
public class DistrictSequence { // Changed from OfficeSequence

    @Id
    private String id; // Format: districtId or districtId-branchId

    @Column(name = "current_max_serial", nullable = false)
    private Long currentMaxSerial = 0L;

    // Allow multiple sequences per district (one per branch) so use ManyToOne
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "district_id") // Changed from office_id
    private District district; // Changed from Office

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id") // Changed from department_id
    private Branch branch; // Changed from Department

    public DistrictSequence() {
    }

    public DistrictSequence(District district, Long currentMaxSerial) { // Changed from Office
        this.district = district;
        this.currentMaxSerial = currentMaxSerial;
        if (district != null)
            this.id = district.getId();
    }

    public DistrictSequence(District district, Branch branch, Long currentMaxSerial) { // Changed from Office,
                                                                                       // Department
        this.district = district;
        this.branch = branch;
        this.currentMaxSerial = currentMaxSerial;
        if (district != null) {
            if (branch != null)
                this.id = district.getId() + "-" + branch.getId();
            else
                this.id = district.getId();
        }
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Long getCurrentMaxSerial() {
        return currentMaxSerial;
    }

    public void setCurrentMaxSerial(Long currentMaxSerial) {
        this.currentMaxSerial = currentMaxSerial;
    }

    public District getDistrict() { // Changed from getOffice
        return district;
    }

    public void setDistrict(District district) { // Changed from setOffice
        this.district = district;
        if (district != null && this.id == null) {
            if (this.branch != null)
                this.id = district.getId() + "-" + this.branch.getId();
            else
                this.id = district.getId();
        }
    }

    public Branch getBranch() { // Changed from getDepartment
        return branch;
    }

    public void setBranch(Branch branch) { // Changed from setDepartment
        this.branch = branch;
        if (this.district != null) {
            if (branch != null)
                this.id = this.district.getId() + "-" + branch.getId();
            else
                this.id = this.district.getId();
        }
    }

    // @PrePersist
    // private void ensureId() {
    //     if (this.id == null && this.district != null) {
    //         if (this.branch != null)
    //             this.id = this.district.getId() + "-" + this.branch.getId();
    //         else
    //             this.id = this.district.getId();
    //     }
    // }
}