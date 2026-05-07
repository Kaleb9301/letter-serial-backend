package com.bankofabyssinia.letter_serial_backend.controller;

import com.bankofabyssinia.letter_serial_backend.dto.Response.DistrictSummaryDTO;
import com.bankofabyssinia.letter_serial_backend.entity.District;
import com.bankofabyssinia.letter_serial_backend.service.DistrictService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/districts")
public class DistrictController {
    private final DistrictService districtService;

    public DistrictController(DistrictService districtService) {
        this.districtService = districtService;
    }

    @GetMapping
    public ResponseEntity<List<DistrictSummaryDTO>> getAllDistricts() {
        List<DistrictSummaryDTO> districts = districtService.getAllDistrictSummaries();
        return ResponseEntity.ok(districts);
    }

    @GetMapping("/summary")
    public ResponseEntity<List<DistrictSummaryDTO>> getAllDistrictSummaries() {
        List<DistrictSummaryDTO> summaries = districtService.getAllDistrictSummaries();
        return ResponseEntity.ok(summaries);
    }

    @PostMapping
    public ResponseEntity<?> createDistrict(@Valid @RequestBody DistrictRequest request) {
        try {
            District district = districtService.createDistrict(
                    request.getName(),
                    request.getCode(),
                    request.getDescription());
            return ResponseEntity.ok(district);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed to create district");
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<District> getDistrict(@PathVariable String id) {
        return districtService.getDistrictById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateDistrict(@PathVariable String id, @Valid @RequestBody DistrictRequest request) {
        try {
            District district = districtService.updateDistrict(
                    id,
                    request.getName(),
                    request.getCode(),
                    request.getDescription());
            return ResponseEntity.ok(district);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed to update district");
        }
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<?> toggleDistrictStatus(@PathVariable String id, @RequestBody StatusRequest request) {
        try {
            if (request.isActive()) {
                District activatedDistrict = districtService.activateDistrict(id);
                return ResponseEntity.ok(activatedDistrict);
            } else {
                districtService.deactivateDistrict(id);
                return ResponseEntity.noContent().build();
            }
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed to update district status: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteDistrict(@PathVariable String id) {
        try {
            districtService.deactivateDistrict(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed to deactivate district");
        }
    }

    // Inner classes
    public static class DistrictRequest {
        private String name;
        private String code;
        private String description;

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
    }

    public static class StatusRequest {
        private boolean active;

        public boolean isActive() {
            return active;
        }

        public void setActive(boolean active) {
            this.active = active;
        }
    }
}