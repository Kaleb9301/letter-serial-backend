package com.bankofabyssinia.letter_serial_backend.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bankofabyssinia.letter_serial_backend.dto.Response.BranchResponse;
import com.bankofabyssinia.letter_serial_backend.entity.Branch;
import com.bankofabyssinia.letter_serial_backend.service.BranchService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api")
public class BranchController {
    private static final Logger log = LoggerFactory.getLogger(BranchController.class);

    private final BranchService branchService;

    public BranchController(BranchService branchService) {
        this.branchService = branchService;
    }

    @PostMapping("/districts/{districtId}/branches")
    public ResponseEntity<?> createBranch(@PathVariable String districtId,
            @Valid @RequestBody BranchRequest request) {
        try {
            Branch branch = branchService.createBranch(
                    districtId,
                    request.getName(),
                    request.getCode(),
                    request.getDescription());
            return ResponseEntity.ok(new BranchResponse(branch));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            log.error("Error creating branch: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new ErrorResponse("Failed to create branch: " + e.getMessage()));
        }
    }

    @GetMapping("/districts/{districtId}/branches")
    public ResponseEntity<?> list(@PathVariable String districtId) {
        try {
            List<Branch> branches = branchService.getBranchesByDistrict(districtId);
            List<BranchResponse> response = branches.stream()
                    .map(BranchResponse::new)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            log.error("Error loading branches: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(new ErrorResponse("Failed to load branches"));
        }
    }

    @PutMapping("/branches/{id}")
    public ResponseEntity<?> update(@PathVariable String id, @Valid @RequestBody BranchRequest request) {
        try {
            Branch branch = branchService.updateBranch(
                    id,
                    request.getName(),
                    request.getCode(),
                    request.getDescription());
            return ResponseEntity.ok(new BranchResponse(branch));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            log.error("Error updating branch: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(new ErrorResponse("Failed to update branch"));
        }
    }

    @DeleteMapping("/branches/{id}")
    public ResponseEntity<?> delete(@PathVariable String id) {
        try {
            branchService.deleteBranch(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(400).body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            log.error("Error deleting branch: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(new ErrorResponse("Failed to delete branch"));
        }
    }

    // Inner class for request body
    public static class BranchRequest {
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

    // Inner class for error response
    public static class ErrorResponse {
        private String message;

        public ErrorResponse(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }
}