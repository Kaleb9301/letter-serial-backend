package com.bankofabyssinia.letter_serial_backend.controller;

import com.bankofabyssinia.letter_serial_backend.dto.Request.CreateLetterRequest;
import com.bankofabyssinia.letter_serial_backend.dto.Request.VoidLetterRequest;
import com.bankofabyssinia.letter_serial_backend.dto.Response.CombinedLetterDTO;
import com.bankofabyssinia.letter_serial_backend.dto.Response.VoidLetterResponse;
import com.bankofabyssinia.letter_serial_backend.entity.Letter;
import com.bankofabyssinia.letter_serial_backend.service.LetterService;
import com.bankofabyssinia.letter_serial_backend.service.AuthenticationService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
// import org.springframework.security.core.Authentication;
// import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;



import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import com.bankofabyssinia.letter_serial_backend.service.UserService;

@RestController
@RequestMapping("/api/letters")
public class LetterController {
    private static final Logger log = LoggerFactory.getLogger(LetterController.class);

    private final LetterService letterService;
    private final AuthenticationService authService;
    private final UserService userService;

    public LetterController(LetterService letterService, AuthenticationService authService, UserService userService) {
        this.letterService = letterService;
        this.authService = authService;
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<?> createLetter(@Valid @RequestBody CreateLetterRequest request) {
        try {
            log.info("Received Letter Memo creation request: {}", request);

            // Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            // if (auth != null) {
            // log.info("Authentication principal class: {}",
            // auth.getPrincipal().getClass().getName());
            // log.info("Authentication principal: {}", auth.getPrincipal());
            // }

            // Get clean user information from JWT
            String userDistrictId = authService.getCurrentUserDistrictId(); // From JWT token
            String cleanUserName = authService.getCurrentUserName();

            log.info("Creating Letter Memo for user: {}, district: {}", cleanUserName, userDistrictId);

            // Pass districtId from JWT, not from request
            Letter letter = letterService.createLetter(request, cleanUserName, userDistrictId);

            // Return DTO with display serial
            Map<String, Object> response = new HashMap<>();
            response.put("id", letter.getId());
            response.put("serialNumber", letter.getSerialNumberValue());
            response.put("serialNumberDisplay", letter.getSerialNumberDisplay());
            response.put("writer", letter.getWriter());
            response.put("recipient", letter.getRecipient());
            response.put("subject", letter.getSubject());
            response.put("letterDate", letter.getLetterDate());
            response.put("districtName", letter.getDistrict() != null ? letter.getDistrict().getName() : null);
            response.put("branchName", letter.getBranchName());
            response.put("referenceNumber", letter.getReferenceNumber());
            response.put("status",
                    letter.getSerialNumber() != null ? letter.getSerialNumber().getStatus().name() : null);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error creating letter: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "status", "error",
                    "message", "Failed to create letter: " + e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<Page<CombinedLetterDTO>> getLetters(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String writer,
            @RequestParam(required = false) String districtId,
            Pageable pageable) {

        boolean isAdmin = authService.hasRole("ROLE_ADMIN");

        String branchId = null;

        if (!isAdmin) {
            try {
                String email = authService.getCurrentUserEmail();
                if (email != null && !email.trim().isEmpty()) {
                    Optional<String> branchIdOpt = userService.findBranchIdByEmail(email.trim());
                    branchId = branchIdOpt.orElse(null);
                }
            } catch (Exception e) {
                log.warn("Could not determine current user's branch ID: {}", e.getMessage());
            }
        }

        Page<CombinedLetterDTO> letters = letterService.getCombinedLettersWithFilters(
                search, status, startDate, endDate, writer, districtId, branchId, pageable);

        return ResponseEntity.ok(letters);
    }

    @PutMapping("/{id}/void")
    public ResponseEntity<?> voidLetter(@PathVariable Long id, @Valid @RequestBody VoidLetterRequest request) {
        try {
            log.info("Voiding Letter Memo ID: {}", id);

            String userDistrictId = authService.getCurrentUserDistrictId(); // Changed from getCurrentUserOfficeId

            // Return DTO instead of entity
            VoidLetterResponse response = letterService.voidLetter(id, request, userDistrictId); // Changed param name

            return ResponseEntity.ok(response);

        } catch (IllegalStateException | IllegalArgumentException | SecurityException e) {
            log.error("Error voiding letter {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "error",
                    "message", e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error voiding letter {}: {}", id, e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "status", "error",
                    "message", "Failed to void letter: " + e.getMessage()));
        }
    }

    @GetMapping("/serial-status")
    public ResponseEntity<?> getSerialStatus() {
        try {
            Map<String, Long> serialStatus = letterService.getSerialStatus();
            return ResponseEntity.ok(serialStatus);
        } catch (Exception e) {
            log.error("Error getting serial status: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "status", "error",
                    "message", "Failed to get serial status: " + e.getMessage()));
        }
    }
}