package com.bankofabyssinia.letter_serial_backend.controller;

import com.bankofabyssinia.letter_serial_backend.dto.UserDTO;
import com.bankofabyssinia.letter_serial_backend.service.UserService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class UserController {
    private static final Logger log = LoggerFactory.getLogger(UserController.class);
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/users")
    public ResponseEntity<?> createUser(@Valid @RequestBody UserDTO dto) {
        try {
            UserDTO createdUser = userService.createUser(dto);
            // Return minimal response without sensitive data
            var response = createMinimalUserResponse(createdUser);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.warn("Validation error creating user: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            log.error("Error creating user: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error creating user: " + e.getMessage());
        }
    }

    @GetMapping("/users")
    public ResponseEntity<List<UserDTO>> listUsers() {
        try {
            List<UserDTO> users = userService.getAllUsersWithDetails();
            // Remove sensitive data from list response
            List<UserDTO> safeUsers = users.stream()
                    .map(this::createMinimalUserResponse)
                    .toList();
            return ResponseEntity.ok(safeUsers);
        } catch (Exception e) {
            log.error("Error listing users with details: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<UserDTO> getUser(@PathVariable String id) {
        try {
            return userService.getUserById(id)
                    .map(this::createMinimalUserResponse)
                    .map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.notFound().build());
        } catch (Exception e) {
            log.error("Error getting user {}: {}", id, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PutMapping("/users/{id}")
    public ResponseEntity<?> updateUser(@PathVariable String id, @Valid @RequestBody UserDTO dto) {
        try {
            UserDTO updatedUser = userService.updateUser(id, dto);
            // Return minimal response
            var response = createMinimalUserResponse(updatedUser);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.warn("Validation error updating user {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            log.error("Error updating user {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error updating user: " + e.getMessage());
        }
    }

    @PatchMapping("/users/{id}/freeze")
    public ResponseEntity<?> freezeUnfreeze(@PathVariable String id, @RequestParam boolean freeze) {
        try {
            UserDTO updatedUser = userService.freezeUser(id, freeze);
            // Return minimal response
            var response = createMinimalUserResponse(updatedUser);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.warn("Error freezing/unfreezing user {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            log.error("Error freezing/unfreezing user {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error updating user status: " + e.getMessage());
        }
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<?> softDelete(@PathVariable String id) {
        try {
            userService.softDeleteUser(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            log.warn("Error soft deleting user {}: {}", id, e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error soft deleting user {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error deleting user: " + e.getMessage());
        }
    }

    @PatchMapping("/users/{id}/activate")
    public ResponseEntity<?> activateUser(@PathVariable String id) {
        try {
            UserDTO updatedUser = userService.activateUser(id);
            // Return minimal response
            var response = createMinimalUserResponse(updatedUser);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.warn("Error activating user {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            log.error("Error activating user {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error activating user: " + e.getMessage());
        }
    }

    /**
     * Create a minimal user response without sensitive data
     */
    private UserDTO createMinimalUserResponse(UserDTO user) {
        UserDTO minimal = new UserDTO();
        minimal.setId(user.getId());
        minimal.setName(user.getName());
        minimal.setEmail(user.getEmail());
        minimal.setRole(user.getRole());
        minimal.setDisplayRole(user.getDisplayRole());
        minimal.setDistrictId(user.getDistrictId());
        minimal.setDistrictName(user.getDistrictName());
        minimal.setBranchId(user.getBranchId());
        minimal.setBranchName(user.getBranchName());
        minimal.setIsActive(user.getIsActive());
        minimal.setIsFirstTime(user.getIsFirstTime());
        minimal.setCreatedAt(user.getCreatedAt());
        // Explicitly set password to null
        minimal.setPassword(null);
        return minimal;
    }
}