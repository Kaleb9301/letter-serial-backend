package com.bankofabyssinia.letter_serial_backend.service;

import com.bankofabyssinia.letter_serial_backend.dto.Response.DistrictSummaryDTO;
import com.bankofabyssinia.letter_serial_backend.entity.Branch;
import com.bankofabyssinia.letter_serial_backend.entity.District;
import com.bankofabyssinia.letter_serial_backend.repository.BranchRepository;
import com.bankofabyssinia.letter_serial_backend.repository.DistrictRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class DistrictService {
    private static final Logger log = LoggerFactory.getLogger(DistrictService.class);

    private final DistrictRepository districtRepository;
    private final BranchRepository branchRepository;

    public DistrictService(DistrictRepository districtRepository, BranchRepository branchRepository) {
        this.districtRepository = districtRepository;
        this.branchRepository = branchRepository;
    }

    @Transactional(readOnly = true)
    public List<District> getAllDistricts() {
        try {
            return districtRepository.findAll();
        } catch (Exception e) {
            log.error("Error getting all districts: {}", e.getMessage(), e);
            return List.of();
        }
    }

    @Transactional(readOnly = true)
    public List<District> getAllActiveDistricts() {
        try {
            return districtRepository.findAllActive();
        } catch (Exception e) {
            log.error("Error getting active districts: {}", e.getMessage(), e);
            return List.of();
        }
    }

    @Transactional(readOnly = true)
    public List<DistrictSummaryDTO> getAllDistrictSummaries() {
        try {
            List<District> districts = districtRepository.findAll();
            return districts.stream()
                    .map(DistrictSummaryDTO::new)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error getting district summaries: {}", e.getMessage(), e);
            return List.of();
        }
    }

    @Transactional
    public District createDistrict(String name, String code, String description) {
        try {
            // Check if district with same code already exists
            if (districtRepository.existsByCode(code)) {
                throw new IllegalArgumentException("District with code " + code + " already exists");
            }

            District district = new District();
            district.setName(name.trim());
            district.setCode(code.trim().toUpperCase());
            district.setDescription(description != null ? description.trim() : null);
            district.setActive(true);

            // Save the district first
            District saved = districtRepository.save(district);

            // Create default branch using the constructor that sets the bidirectional
            // relationship
            String defaultPrefix = (saved.getCode() + "-DEFAULT").toUpperCase();
            Branch defaultBranch = new Branch("Default", defaultPrefix, true, saved);

            // Save the branch
            branchRepository.save(defaultBranch);

            // The branch is already added to district's branches list via the constructor
            // But we need to refresh the district to get the updated branches list
            saved = districtRepository.save(saved);

            log.info("District created: {} ({}) with default branch", saved.getName(), saved.getCode());

            return saved;
        } catch (IllegalArgumentException e) {
            log.warn("Validation error creating district: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error creating district: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create district: " + e.getMessage(), e);
        }
    }

    @Transactional(readOnly = true)
    public Optional<District> getDistrictById(String id) {
        try {
            return districtRepository.findById(id);
        } catch (Exception e) {
            log.error("Error getting district by ID {}: {}", id, e.getMessage());
            return Optional.empty();
        }
    }

    @Transactional
    public District updateDistrict(String id, String name, String code, String description) {
        try {
            Optional<District> opt = districtRepository.findById(id);
            if (opt.isEmpty()) {
                throw new IllegalArgumentException("District not found: " + id);
            }

            District district = opt.get();

            // Check if code is being changed and if new code already exists
            if (!district.getCode().equals(code)) {
                if (districtRepository.existsByCodeAndIdNot(code, id)) {
                    throw new IllegalArgumentException("District with code " + code + " already exists");
                }
            }

            district.setName(name.trim());
            district.setCode(code.trim().toUpperCase());
            district.setDescription(description != null ? description.trim() : null);

            District updated = districtRepository.save(district);
            log.info("District updated: {} ({})", updated.getName(), updated.getCode());

            return updated;
        } catch (IllegalArgumentException e) {
            log.warn("Validation error updating district: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error updating district {}: {}", id, e.getMessage(), e);
            throw new RuntimeException("Failed to update district: " + e.getMessage(), e);
        }
    }

    @Transactional
    public District activateDistrict(String id) {
        try {
            Optional<District> opt = districtRepository.findById(id);
            if (opt.isEmpty()) {
                throw new IllegalArgumentException("District not found: " + id);
            }

            District district = opt.get();
            district.setActive(true);

            District activated = districtRepository.save(district);
            log.info("District activated: {} ({})", activated.getName(), activated.getCode());

            return activated;
        } catch (IllegalArgumentException e) {
            log.warn("Validation error activating district: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error activating district {}: {}", id, e.getMessage(), e);
            throw new RuntimeException("Failed to activate district: " + e.getMessage(), e);
        }
    }

    @Transactional
    public void deactivateDistrict(String id) {
        try {
            Optional<District> opt = districtRepository.findById(id);
            if (opt.isEmpty()) {
                throw new IllegalArgumentException("District not found: " + id);
            }

            District district = opt.get();
            district.setActive(false);

            districtRepository.save(district);
            log.info("District deactivated: {} ({})", district.getName(), district.getCode());

        } catch (IllegalArgumentException e) {
            log.warn("Validation error deactivating district: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error deactivating district {}: {}", id, e.getMessage(), e);
            throw new RuntimeException("Failed to deactivate district: " + e.getMessage(), e);
        }
    }

    @Transactional(readOnly = true)
    public Optional<District> findByCode(String code) {
        try {
            return districtRepository.findByCode(code);
        } catch (Exception e) {
            log.error("Error finding district by code {}: {}", code, e.getMessage());
            return Optional.empty();
        }
    }

    @Transactional(readOnly = true)
    public List<Branch> getDistrictBranches(String districtId) {
        try {
            Optional<District> districtOpt = districtRepository.findById(districtId);
            if (districtOpt.isEmpty()) {
                throw new IllegalArgumentException("District not found: " + districtId);
            }
            return districtOpt.get().getBranches();
        } catch (IllegalArgumentException e) {
            log.warn("Validation error getting district branches: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error getting branches for district {}: {}", districtId, e.getMessage(), e);
            return List.of();
        }
    }
}