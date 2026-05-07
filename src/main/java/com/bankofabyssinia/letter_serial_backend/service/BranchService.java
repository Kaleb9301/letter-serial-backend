package com.bankofabyssinia.letter_serial_backend.service;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bankofabyssinia.letter_serial_backend.entity.Branch;
import com.bankofabyssinia.letter_serial_backend.entity.District;
import com.bankofabyssinia.letter_serial_backend.repository.BranchRepository;
import com.bankofabyssinia.letter_serial_backend.repository.DistrictRepository;

@Service
public class BranchService {
    private static final Logger log = LoggerFactory.getLogger(BranchService.class);

    private final BranchRepository branchRepository;
    private final DistrictRepository districtRepository;

    public BranchService(BranchRepository branchRepository, DistrictRepository districtRepository) {
        this.branchRepository = branchRepository;
        this.districtRepository = districtRepository;
    }

    @Transactional
    public Branch createBranch(String districtId, String name, String code, String description) {
        try {
            Optional<District> districtOpt = districtRepository.findById(districtId);
            if (districtOpt.isEmpty()) {
                throw new IllegalArgumentException("District not found: " + districtId);
            }

            District district = districtOpt.get();

            // Check if branch with same name already exists in this district
            if (branchRepository.existsByDistrictIdAndNameIgnoreCase(districtId, name)) {
                throw new IllegalArgumentException("Branch with name '" + name + "' already exists in this district");
            }

            // Generate unique prefix
            String basePrefix = district.getCode() + "-" +
                    name.replaceAll("[^a-zA-Z0-9\\s]", "").trim().replaceAll("\\s+", "-").toUpperCase();
            String prefix = basePrefix;

            // Check if prefix already exists and append number if needed
            int counter = 1;
            while (branchRepository.existsByPrefix(prefix)) {
                prefix = basePrefix + "-" + counter;
                counter++;

                if (counter > 100) {
                    throw new RuntimeException("Unable to generate unique prefix after 100 attempts");
                }
            }

            // Check if this is the first branch in the district
            boolean isDefault = branchRepository.countByDistrictId(districtId) == 0;

            // Create branch WITHOUT using the constructor that tries to modify district
            Branch branch = new Branch();
            branch.setName(name);
            branch.setCode(code != null ? code.trim() : null);
            branch.setDescription(description != null ? description.trim() : null);
            branch.setPrefix(prefix);
            branch.setDefault(isDefault);
            branch.setDistrict(district);

            Branch saved = branchRepository.save(branch);
            log.info("Branch created: {} for district: {}", saved.getName(), district.getName());

            return saved;
        } catch (IllegalArgumentException e) {
            log.warn("Validation error creating branch: {}", e.getMessage());
            throw e;
        } catch (RuntimeException e) {
            log.error("Error creating branch: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create branch: " + e.getMessage(), e);
        }
    }

    @Transactional
    public Branch updateBranch(String id, String name, String code, String description) {
        try {
            Optional<Branch> opt = branchRepository.findById(id);
            if (opt.isEmpty()) {
                throw new IllegalArgumentException("Branch not found: " + id);
            }

            Branch branch = opt.get();
            String districtId = branch.getDistrict().getId();

            // Check if new name already exists in the same district
            if (!branch.getName().equalsIgnoreCase(name) &&
                    branchRepository.existsByDistrictIdAndNameIgnoreCase(districtId, name)) {
                throw new IllegalArgumentException("Branch with name '" + name + "' already exists in this district");
            }

            // Update the prefix if name changes
            if (!branch.getName().equals(name)) {
                String basePrefix = branch.getDistrict().getCode() + "-" +
                        name.replaceAll("[^a-zA-Z0-9\\s]", "").trim().replaceAll("\\s+", "-").toUpperCase();
                String newPrefix = basePrefix;

                // Check if new prefix already exists (excluding current branch)
                int counter = 1;
                while (branchRepository.existsByPrefixAndIdNot(newPrefix, id)) {
                    newPrefix = basePrefix + "-" + counter;
                    counter++;

                    if (counter > 100) {
                        throw new RuntimeException("Unable to generate unique prefix after 100 attempts");
                    }
                }

                branch.setPrefix(newPrefix);
            }

            branch.setName(name);
            branch.setCode(code != null ? code.trim() : null);
            branch.setDescription(description != null ? description.trim() : null);

            Branch updated = branchRepository.save(branch);
            log.info("Branch updated: {}", updated.getName());

            return updated;
        } catch (IllegalArgumentException e) {
            log.warn("Validation error updating branch: {}", e.getMessage());
            throw e;
        } catch (RuntimeException e) {
            log.error("Error updating branch {}: {}", id, e.getMessage(), e);
            throw new RuntimeException("Failed to update branch: " + e.getMessage(), e);
        }
    }

    @Transactional(readOnly = true)
    public List<Branch> getBranchesByDistrict(String districtId) {
        try {
            Optional<District> districtOpt = districtRepository.findById(districtId);
            if (districtOpt.isEmpty()) {
                throw new IllegalArgumentException("District not found: " + districtId);
            }

            return branchRepository.findByDistrictId(districtId);
        } catch (IllegalArgumentException e) {
            log.warn("Validation error getting branches: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error getting branches for district {}: {}", districtId, e.getMessage(), e);
            return List.of();
        }
    }

    @Transactional
    public void deleteBranch(String id) {
        try {
            Optional<Branch> opt = branchRepository.findById(id);
            if (opt.isEmpty()) {
                throw new IllegalArgumentException("Branch not found: " + id);
            }

            Branch branch = opt.get();
            if (branch.isDefault()) {
                throw new IllegalArgumentException("Cannot delete default branch");
            }

            // Remove branch from district's branches list
            District district = branch.getDistrict();
            if (district != null) {
                district.getBranches().remove(branch);
            }

            branchRepository.delete(branch);
            log.info("Branch deleted: {}", branch.getName());

        } catch (IllegalArgumentException e) {
            log.warn("Validation error deleting branch: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error deleting branch {}: {}", id, e.getMessage(), e);
            throw new RuntimeException("Failed to delete branch: " + e.getMessage(), e);
        }
    }

    @Transactional(readOnly = true)
    public Optional<Branch> getDefaultBranch(District district) {
        try {
            return branchRepository.findByDistrictAndIsDefaultTrue(district);
        } catch (Exception e) {
            log.error("Error getting default branch for district {}: {}", district.getId(), e.getMessage());
            return Optional.empty();
        }
    }

    @Transactional(readOnly = true)
    public Optional<Branch> findById(String id) {
        try {
            return branchRepository.findById(id);
        } catch (Exception e) {
            log.error("Error finding branch by ID {}: {}", id, e.getMessage());
            return Optional.empty();
        }
    }

    @Transactional(readOnly = true)
    public List<Branch> getAllActiveBranches() {
        try {
            return branchRepository.findAllActiveBranches();
        } catch (Exception e) {
            log.error("Error getting all active branches: {}", e.getMessage(), e);
            return List.of();
        }
    }

    @Transactional(readOnly = true)
    public Optional<Branch> getDefaultByDistrictId(String districtId) {
        try {
            return branchRepository.findDefaultByDistrictId(districtId);
        } catch (Exception e) {
            log.error("Error getting default branch for district ID {}: {}", districtId, e.getMessage());
            return Optional.empty();
        }
    }
}