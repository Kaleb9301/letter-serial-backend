package com.bankofabyssinia.letter_serial_backend.service;

import com.bankofabyssinia.letter_serial_backend.dto.Request.CreateLetterRequest;
import com.bankofabyssinia.letter_serial_backend.dto.Response.CombinedLetterDTO;
import com.bankofabyssinia.letter_serial_backend.dto.Response.LetterWithDetails;
import com.bankofabyssinia.letter_serial_backend.dto.Response.LetterWithStatusDTO;
import com.bankofabyssinia.letter_serial_backend.dto.Request.LettersFilter;
import com.bankofabyssinia.letter_serial_backend.dto.Request.VoidLetterRequest;
import com.bankofabyssinia.letter_serial_backend.dto.Response.VoidLetterResponse;
import com.bankofabyssinia.letter_serial_backend.entity.*;
import com.bankofabyssinia.letter_serial_backend.enums.AuditAction;
import com.bankofabyssinia.letter_serial_backend.enums.EntityType;
import com.bankofabyssinia.letter_serial_backend.enums.SerialNumberStatus;
import com.bankofabyssinia.letter_serial_backend.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class LetterService {
    private static final Logger log = LoggerFactory.getLogger(LetterService.class);

    private final LetterRepository letterRepository;
    private final SerialNumberRepository serialNumberRepository;
    private final DistrictRepository districtRepository;
    private final DistrictSequenceRepository sequenceRepository;
    private final BranchRepository branchRepository;
    private final AuditLogRepository auditLogRepository;
    private final AuthenticationService authenticationService;
    private final UserRepository userRepository;
    

    public LetterService(LetterRepository letterRepository, SerialNumberRepository serialNumberRepository,
            DistrictRepository districtRepository, DistrictSequenceRepository sequenceRepository,
            VoidedLetterRepository voidedLetterRepository,
            AuditLogRepository auditLogRepository, AuthenticationService authenticationService,
            UserRepository userRepository,
            BranchRepository branchRepository) {
        this.letterRepository = letterRepository;
        this.serialNumberRepository = serialNumberRepository;
        this.districtRepository = districtRepository;
        this.sequenceRepository = sequenceRepository;
        this.auditLogRepository = auditLogRepository;
        this.authenticationService = authenticationService;
        this.userRepository = userRepository;
        this.branchRepository = branchRepository;
    }

    public Letter createLetter(CreateLetterRequest request, String cleanUserName, String userDistrictId) {
        try {
            // Use userDistrictId parameter instead of request.getDistrictId()
            log.info("Creating Letter Memo for district: {}, user: {}, userDistrictId: {}",
                    userDistrictId, cleanUserName, userDistrictId); // Changed request.getDistrictId() to userDistrictId

            // Validate reference number uniqueness within the district (case-insensitive)
            // Use userDistrictId instead of request.getDistrictId()
            if (letterRepository.existsByDistrict_IdAndReferenceNumberIgnoreCase(userDistrictId,
                    request.getReferenceNumber())) {
                throw new IllegalArgumentException("Reference number already exists for this district");
            }

            Optional<District> districtOpt = districtRepository.findById(userDistrictId); // Changed to userDistrictId
            if (districtOpt.isEmpty()) {
                throw new IllegalArgumentException("District not found: " + userDistrictId); // Changed to
                                                                                             // userDistrictId
            }
            District district = districtOpt.get();

            // District access check - userDistrictId comes from JWT token
            // No need to compare with request.getDistrictId() since we removed it
            // Just ensure user has access to this district
            // Note: The check happens in the controller layer

            String currentEmailForSerial = cleanUserName;

            Branch branchForSerial = null;
            if (request.getBranchId() != null && !request.getBranchId().isBlank()) {
                try {
                    branchForSerial = branchRepository.findById(request.getBranchId()).orElse(null);
                } catch (Exception e) {
                    log.debug("Could not resolve requested branch {}: {}", request.getBranchId(),
                            e.getMessage());
                }
            }

            if (branchForSerial == null) {
                try {
                    String currentEmail = authenticationService.getCurrentUserEmail();
                    if (currentEmail != null) {
                        var userOpt = userRepository.findByEmail(currentEmail.toLowerCase().trim());
                        if (userOpt.isPresent() && userOpt.get().getBranch() != null) {
                            branchForSerial = userOpt.get().getBranch();
                        }
                    }
                } catch (Exception e) {
                    log.debug("Could not resolve user branch while creating letter: {}", e.getMessage());
                }
            }

            // Get the next serial number using branch-aware method
            SerialNumber serial = getNextSerialNumber(district, branchForSerial, currentEmailForSerial);
            log.info("Obtained serial number: {} for district: {}", serial.getSerialNumber(), district.getId());

            LocalDateTime now = LocalDateTime.now();

            // Convert LocalDate to LocalDateTime for letterDate
            LocalDateTime letterDateTime = request.getLetterDate().atStartOfDay();

            // Create the letter with clean user name
            Letter letter = new Letter(
                    serial, // Pass the SerialNumber entity
                    cleanUserName, // Use clean name passed from controller
                    request.getRecipient(),
                    request.getSubject(),
                    letterDateTime, // Use LocalDateTime
                    district);
            letter.setReferenceNumber(request.getReferenceNumber().trim());

            // If the request provided branch info (frontend), use it. Otherwise
            // populate branch information from the branch used for serial resolution or the
            // current user.
            if (request.getBranchId() != null && !request.getBranchId().isBlank()) {
                letter.setBranchId(request.getBranchId());
                letter.setBranchName(request.getBranchName());
            } else if (branchForSerial != null) {
                letter.setBranchId(branchForSerial.getId());
                letter.setBranchName(branchForSerial.getName());
            } else {
                try {
                    String currentEmail = authenticationService.getCurrentUserEmail();
                    if (currentEmail != null) {
                        userRepository.findByEmail(currentEmail.toLowerCase().trim()).ifPresent(user -> {
                            if (user.getBranch() != null) {
                                letter.setBranchId(user.getBranch().getId());
                                letter.setBranchName(user.getBranch().getName());
                            }
                        });
                    }
                } catch (Exception e) {
                    log.debug("Could not resolve user branch while creating letter: {}", e.getMessage());
                }
            }

            Letter savedLetter = letterRepository.save(letter);
            log.info("Successfully created letter with ID: {} and serial: {}",
                    savedLetter.getId(), serial.getSerialNumber());

            auditLogRepository.save(new AuditLog(
                    EntityType.LETTER,
                    savedLetter.getId(),
                    AuditAction.CREATE,
                    null,
                    cleanUserName,
                    now,
                    district));

            AuditAction serialAction = serial.getStatus() == SerialNumberStatus.USED && serial.getVoidedBy() != null
                    ? AuditAction.REUSE
                    : AuditAction.CREATE;
            auditLogRepository.save(new AuditLog(
                    EntityType.SERIAL,
                    serial.getSerialNumber(),
                    serialAction,
                    null,
                    cleanUserName,
                    now,
                    district));

            return savedLetter;
        } catch (DataIntegrityViolationException e) {
            log.error("Database integrity violation during letter creation: {}", e.getMessage());
            throw new IllegalStateException("Serial number conflict - please try again or contact admin.");
        } catch (RuntimeException e) {
            log.error("Unexpected error creating letter: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create letter due to internal error.", e);
        }
    }

    @Transactional
    @SuppressWarnings("UseSpecificCatch")
    public VoidLetterResponse voidLetter(Long id, VoidLetterRequest request, String userDistrictId) {
        try {
            log.info("Voiding Letter Memo ID: {}", id);

            Optional<Letter> letterOpt = letterRepository.findById(id);
            if (letterOpt.isEmpty()) {
                throw new IllegalArgumentException("Letter not found: " + id);
            }
            Letter letter = letterOpt.get();

            if (!letter.getDistrict().getId().equals(userDistrictId)) {
                throw new SecurityException("Access denied to letter: " + id + " - district mismatch");
            }

            if (!isLatestLetter(letter)) {
                throw new IllegalStateException("Only the latest letter can be voided.");
            }

            String performedBy = authenticationService.getCurrentUserName();

            String voidReason = request.getReason();
            if (voidReason != null && voidReason.length() > 500) {
                voidReason = voidReason.substring(0, 500);
            }

            letter.setIsVoided(true);
            letter.setVoidReason(voidReason);
            letter.setVoidedAt(LocalDateTime.now());
            letter.setVoidedBy(performedBy);

            SerialNumber serial = letter.getSerialNumber();
            serial.setStatus(SerialNumberStatus.AVAILABLE);
            serial.setVoidedBy(performedBy);
            serial.setVoidedAt(LocalDateTime.now());
            serial.setVoidReason(voidReason);

            Letter savedLetter = letterRepository.save(letter);
            serialNumberRepository.save(serial);

            updateDistrictSequenceAfterVoid(letter.getDistrict(), letter.getBranchId(), serial.getSerialNumber());

            LocalDateTime now = LocalDateTime.now();
            auditLogRepository.save(new AuditLog(
                    EntityType.LETTER,
                    id,
                    AuditAction.VOID,
                    "Letter voided. Reason: " + voidReason,
                    performedBy,
                    now,
                    letter.getDistrict()));

            auditLogRepository.save(new AuditLog(
                    EntityType.SERIAL,
                    serial.getSerialNumber(),
                    AuditAction.VOID,
                    "Serial marked as AVAILABLE for reuse",
                    performedBy,
                    now,
                    letter.getDistrict()));

            log.info("Successfully voided letter ID: {} and made serial {} AVAILABLE for reuse",
                    id, serial.getSerialNumber());

            // Return DTO instead of entity
            return new VoidLetterResponse(savedLetter);

        } catch (IllegalStateException | IllegalArgumentException | SecurityException e) {
            log.warn("Client error voiding letter {}: {}", id, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error voiding letter {}: {}", id, e.getMessage(), e);
            throw new RuntimeException("Failed to void letter due to internal error.", e);
        }
    }

    private boolean isLatestLetter(Letter letter) {
        try {
            Optional<Long> maxSerialOpt;
            if (letter.getBranchId() != null) {
                maxSerialOpt = letterRepository.findMaxActiveSerialByDistrictAndBranch(letter.getDistrict().getId(),
                        letter.getBranchId());
            } else {
                maxSerialOpt = letterRepository.findMaxActiveSerialByDistrict(letter.getDistrict().getId());
            }

            if (maxSerialOpt.isPresent()) {
                Long currentMaxSerial = maxSerialOpt.get();
                Long letterSerial = letter.getSerialNumber().getSerialNumber();

                log.info("Checking if letter {} with serial {} is latest (max serial: {})",
                        letter.getId(), letterSerial, currentMaxSerial);

                return letterSerial.equals(currentMaxSerial);
            }

            return true;
        } catch (Exception e) {
            log.error("Error checking if letter is latest: {}", e.getMessage());
            return false;
        }
    }

    private void updateDistrictSequenceAfterVoid(District district, String branchId, Long voidedSerialNumber) {
        try {
            String seqId = buildSequenceId(district.getId(), branchId);
            Optional<DistrictSequence> seqOpt = sequenceRepository.findByDistrictIdForUpdate(seqId);
            if (seqOpt.isEmpty())
                return;

            DistrictSequence seq = seqOpt.get();

            if (voidedSerialNumber.equals(seq.getCurrentMaxSerial())) {
                Long newHighestActive = findHighestActiveSerial(district.getId(), branchId);

                if (newHighestActive > 0) {
                    seq.setCurrentMaxSerial(newHighestActive);
                    sequenceRepository.save(seq);
                    log.info("Updated district sequence to {} after voiding serial {}", newHighestActive,
                            voidedSerialNumber);
                }
            }
        } catch (Exception e) {
            log.error("Error updating district sequence after void: {}", e.getMessage());
        }
    }

    @Transactional
    public SerialNumber getNextSerialNumber(District district, String performedBy) {
        return getNextSerialNumber(district, null, performedBy);
    }

    @Transactional
    public SerialNumber getNextSerialNumber(District district, Branch branch, String performedBy) {
        try {
            log.info("Getting next serial number for district: {} branch: {}", district.getId(),
                    branch != null ? branch.getId() : "(none)");

            District managedDistrict = districtRepository.findById(district.getId())
                    .orElseThrow(() -> new IllegalArgumentException("District not found: " + district.getId()));

            String seqId = buildSequenceId(managedDistrict.getId(), branch != null ? branch.getId() : null);
            Optional<DistrictSequence> seqOpt = sequenceRepository.findByDistrictIdForUpdate(seqId);
            DistrictSequence seq;

            if (seqOpt.isPresent()) {
                seq = seqOpt.get();
                if (seq.getDistrict() == null) {
                    seq.setDistrict(managedDistrict);
                }
                if (seq.getBranch() == null && branch != null) {
                    seq.setBranch(branch);
                }
            } else {
                if (branch != null)
                    seq = new DistrictSequence(managedDistrict, branch, 0L);
                else
                    seq = new DistrictSequence(managedDistrict, 0L);
                seq = sequenceRepository.save(seq);
            }

            String branchId = branch != null ? branch.getId() : null;
            List<SerialNumber> availableReusableSerials = findAvailableReusableSerials(managedDistrict.getId(),
                    branchId);

            if (!availableReusableSerials.isEmpty()) {
                SerialNumber highestAvailable = availableReusableSerials.get(0);
                log.info("Reusing available serial: {}", highestAvailable.getSerialNumber());

                highestAvailable.setStatus(SerialNumberStatus.USED);
                highestAvailable.setCreatedBy(performedBy);
                highestAvailable.setCreatedAt(LocalDateTime.now());
                highestAvailable.setVoidedBy(null);
                highestAvailable.setVoidedAt(null);
                highestAvailable.setVoidReason(null);

                SerialNumber savedSerial = serialNumberRepository.save(highestAvailable);
                return savedSerial;
            }

            Long nextSerialNumber = Math.max(
                    findHighestActiveSerial(managedDistrict.getId(), branchId),
                    seq.getCurrentMaxSerial()) + 1;

            log.info("Creating new serial: {}", nextSerialNumber);

            SerialNumber serial = new SerialNumber(
                    nextSerialNumber,
                    SerialNumberStatus.USED,
                    performedBy,
                    LocalDateTime.now(),
                    managedDistrict,
                    branch);

            if (nextSerialNumber > seq.getCurrentMaxSerial()) {
                seq.setCurrentMaxSerial(nextSerialNumber);
                sequenceRepository.save(seq);
            }

            SerialNumber savedSerial = serialNumberRepository.save(serial);
            log.info("Created new serial number: {}", savedSerial.getSerialNumber());
            return savedSerial;

        } catch (DataIntegrityViolationException e) {
            log.error("Database integrity violation during serial generation: {}", e.getMessage());
            throw new IllegalStateException("Serial number generation failed due to conflict - please try again.");
        } catch (Exception e) {
            log.error("Unexpected error generating serial number: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to generate serial number due to internal error.", e);
        }
    }

    private List<SerialNumber> findAvailableReusableSerials(String districtId, String branchId) {
        // Find AVAILABLE serials (voided ones that can be reused) scoped to branch
        // if provided
        List<SerialNumber> availableSerials = serialNumberRepository
                .findByDistrictIdAndBranchIdAndStatusOrderBySerialNumberDesc(districtId, branchId,
                        SerialNumberStatus.AVAILABLE);

        return availableSerials.stream()
                // Only use serials that don't have active (non-voided) letters in the same
                // branch scope
                .filter(serial -> !letterRepository.existsBySerialNumberAndIsVoidedFalse(serial, districtId,
                        branchId))
                .collect(Collectors.toList());
    }

    private Long findHighestActiveSerial(String districtId, String branchId) {
        // Find the highest USED serial number that has an active letter
        // (branch-aware)
        Optional<Long> maxActiveSerial;
        if (branchId != null) {
            maxActiveSerial = letterRepository.findMaxActiveSerialByDistrictAndBranch(districtId, branchId);
        } else {
            maxActiveSerial = letterRepository.findMaxActiveSerialByDistrict(districtId);
        }
        return maxActiveSerial.orElse(0L);
    }

    private String buildSequenceId(String districtId, String branchId) {
        if (branchId == null || branchId.isBlank())
            return districtId;
        return districtId + "-" + branchId;
    }

    public Page<LetterWithStatusDTO> getLetters(LettersFilter filter, Pageable pageable, String userDistrictId,
            boolean isAdmin) {
        try {
            log.info("Getting letters with filter: {}, userDistrictId: {}, isAdmin: {}", filter, userDistrictId,
                    isAdmin);

            if (!isAdmin && (filter.getDistrictId() == null || !filter.getDistrictId().equals(userDistrictId))) {
                filter.setDistrictId(userDistrictId);
            }

            // Auto-apply branch restriction for non-admin users
            if (!isAdmin) {
                try {
                    String currentEmail = authenticationService.getCurrentUserEmail();
                    if (currentEmail != null) {
                        var u = userRepository.findByEmail(currentEmail.toLowerCase().trim());
                        if (u.isPresent() && u.get().getBranch() != null) {
                            filter.setBranchId(u.get().getBranch().getId());
                        }
                    }
                } catch (Exception e) {
                    log.debug("Could not auto-apply branch filter: {}", e.getMessage());
                }
            }

            // Convert status string to enum if provided
            SerialNumberStatus statusEnum = null;
            if (filter.getStatus() != null && !filter.getStatus().isEmpty()) {
                try {
                    statusEnum = SerialNumberStatus.valueOf(filter.getStatus().toUpperCase());
                } catch (IllegalArgumentException e) {
                    log.warn("Invalid status filter '{}': {}", filter.getStatus(), e.getMessage());
                    statusEnum = null;
                }
            }

            // Convert LocalDate to LocalDateTime for repository method
            LocalDateTime startDateTime = filter.getStartDate() != null ? filter.getStartDate().atStartOfDay() : null;
            LocalDateTime endDateTime = filter.getEndDate() != null ? filter.getEndDate().atTime(23, 59, 59) : null;

            Page<Letter> letters = letterRepository.findByFilters(
                    filter.getDistrictId(),
                    filter.getBranchId(),
                    filter.getSearch(),
                    statusEnum,
                    startDateTime,
                    endDateTime,
                    filter.getWriter(),
                    pageable);

            // Convert to DTO - include display serial
            return letters.map(letter -> new LetterWithStatusDTO(
                    letter.getId(),
                    letter.getSerialNumberValue(),
                    letter.getSerialNumberDisplay(), // Add display serial
                    letter.getWriter(),
                    letter.getRecipient(),
                    letter.getSubject(),
                    letter.getLetterDate(),
                    letter.getCreatedAt(),
                    letter.getDistrict().getId(),
                    letter.getDistrict().getName(),
                    letter.getSerialNumber().getStatus().name()));
        } catch (Exception e) {
            log.error("Unexpected error fetching letters with filter: {}", filter, e);
            throw new RuntimeException("Failed to fetch letters due to internal error.", e);
        }
    }

    public List<Letter> getLettersWithFilters(String search, String status, LocalDate startDate,
            LocalDate endDate, String writer, String districtId) {
        try {
            // Convert LocalDate to LocalDateTime
            LocalDateTime startDateTime = startDate != null ? startDate.atStartOfDay() : null;
            LocalDateTime endDateTime = endDate != null ? endDate.atTime(23, 59, 59) : null;

            // Convert status string to enum
            SerialNumberStatus statusEnum = null;
            if (status != null && !status.isEmpty()) {
                try {
                    statusEnum = SerialNumberStatus.valueOf(status.toUpperCase());
                } catch (IllegalArgumentException e) {
                    log.warn("Invalid status filter '{}': {}", status, e.getMessage());
                    statusEnum = null;
                }
            }

            // Use the repository method with proper parameters (no branch context)
            return letterRepository.findByFiltersList(
                    districtId, null, search, statusEnum, startDateTime, endDateTime, writer);
        } catch (Exception e) {
            log.error("Unexpected error fetching letters with filters: search={}, status={}, districtId={}", search,
                    status, districtId, e);
            throw new RuntimeException("Failed to fetch letters due to internal error.", e);
        }
    }

    @SuppressWarnings("UseSpecificCatch")
    public LetterWithDetails getLetterWithDetails(Long id, String userDistrictId, boolean isAdmin) {
        try {
            log.info("Getting letter details for ID: {}", id);

            Optional<Letter> letterOpt = letterRepository.findById(id);
            if (letterOpt.isEmpty()) {
                throw new IllegalArgumentException("Letter not found: " + id);
            }
            Letter letter = letterOpt.get();

            if (!isAdmin && !letter.getDistrict().getId().equals(userDistrictId)) {
                throw new SecurityException("Access denied to letter: " + id);
            }

            SerialNumber serial = letter.getSerialNumber();

            List<AuditLog> audits = auditLogRepository.findByEntityTypeAndEntityId(EntityType.LETTER, id);
            audits.addAll(auditLogRepository.findByEntityTypeAndEntityId(EntityType.SERIAL, serial.getSerialNumber()));

            LetterWithDetails details = new LetterWithDetails();
            details.setLetter(letter);
            details.setSerial(serial);
            details.setAuditLogs(audits);

            log.info("Retrieved details for letter ID: {} with serial: {} (display: {})",
                    id, serial.getSerialNumber(), serial.getDisplaySerial());
            return details;
        } catch (Exception e) {
            log.error("Unexpected error fetching letter details for ID: {}", id, e);
            throw new RuntimeException("Failed to fetch letter details due to internal error.", e);
        }
    }

    public Map<String, Long> getSerialStatus() {
        try {
            log.info("Getting serial status for all districts");

            List<District> districts = districtRepository.findAll();
            Map<String, Long> map = new HashMap<>();

            // Try to scope to current user's branch for branch-aware next serial
            String currentDistrictId = null;
            String currentBranchId = null;
            try {
                String currentEmail = authenticationService.getCurrentUserEmail();
                if (currentEmail != null) {
                    var userOpt = userRepository.findByEmail(currentEmail.toLowerCase().trim());
                    if (userOpt.isPresent()) {
                        if (userOpt.get().getDistrict() != null) {
                            currentDistrictId = userOpt.get().getDistrict().getId();
                        }
                        if (userOpt.get().getBranch() != null) {
                            currentBranchId = userOpt.get().getBranch().getId();
                        }
                    }
                }
            } catch (Exception e) {
                log.debug("Could not resolve current user district/branch: {}", e.getMessage());
            }

            for (District district : districts) {
                Long currentSequence;

                // Branch-aware sequence when applicable (only for the user's district)
                if (currentDistrictId != null && district.getId().equals(currentDistrictId)
                        && currentBranchId != null) {
                    String seqId = district.getId() + "-" + currentBranchId;
                    Optional<DistrictSequence> branchSeq = sequenceRepository.findById(seqId);
                    currentSequence = branchSeq.map(DistrictSequence::getCurrentMaxSerial).orElse(0L);
                } else {
                    // Fallback to district-wide sequence
                    @SuppressWarnings("null")
                    Optional<DistrictSequence> seq = sequenceRepository.findById(district.getId());
                    currentSequence = seq.map(DistrictSequence::getCurrentMaxSerial).orElse(0L);
                }

                // Find the highest USED serial number that has an active letter
                // (branch-aware for current district)
                Optional<Long> maxActiveUsedSerial;
                if (currentDistrictId != null && district.getId().equals(currentDistrictId)
                        && currentBranchId != null) {
                    maxActiveUsedSerial = letterRepository.findMaxActiveSerialByDistrictAndBranch(district.getId(),
                            currentBranchId);
                } else {
                    maxActiveUsedSerial = letterRepository.findMaxActiveSerialByDistrict(district.getId());
                }

                Long highestSerial = Math.max(currentSequence, maxActiveUsedSerial.orElse(0L));

                // Return NEXT serial (current highest + 1)
                map.put(district.getCode(), highestSerial);

                log.info("District {}: sequence={}, maxActive={}, nextSerial={}",
                        district.getCode(), currentSequence, maxActiveUsedSerial.orElse(0L),
                        highestSerial + 1);
            }

            log.info("Final serial status: {}", map);
            return map;
        } catch (Exception e) {
            log.error("Unexpected error fetching serial status: {}", e);
            throw new RuntimeException("Failed to fetch serial status due to internal error.", e);
        }
    }

    @SuppressWarnings("null")
    public Page<CombinedLetterDTO> getCombinedLettersWithFilters(
            String search,
            String status,
            LocalDate startDate,
            LocalDate endDate,
            String writer,
            String districtId,
            String branchId,
            Pageable pageable) {

        try {
            LocalDateTime startDateTime = startDate != null ? startDate.atStartOfDay() : null;
            LocalDateTime endDateTime = endDate != null ? endDate.atTime(23, 59, 59) : null;

            List<CombinedLetterDTO> combinedResults = new ArrayList<>();

            // Get ALL letters without status filter first (branch-aware)
            List<Letter> allLetters = letterRepository.findByFiltersList(
                    districtId, branchId, search, null, startDateTime, endDateTime, writer);

            // Apply status filtering in Java (simple and working)
            List<Letter> filteredLetters = allLetters;
            if (status != null && !status.isEmpty()) {
                filteredLetters = allLetters.stream()
                        .filter(letter -> {
                            if (status.equalsIgnoreCase("VOIDED")) {
                                return letter.getIsVoided(); // Return voided letters
                            } else if (status.equalsIgnoreCase("USED")) {
                                return !letter.getIsVoided() &&
                                        letter.getSerialNumber().getStatus() == SerialNumberStatus.USED;
                            } else if (status.equalsIgnoreCase("AVAILABLE")) {
                                return !letter.getIsVoided() &&
                                        letter.getSerialNumber().getStatus() == SerialNumberStatus.AVAILABLE;
                            }
                            return true; // Unknown status, return all
                        })
                        .collect(Collectors.toList());
            }

            // Convert to DTO
            for (Letter letter : filteredLetters) {
                CombinedLetterDTO dto = new CombinedLetterDTO(letter);
                combinedResults.add(dto);
            }

            // Sort by creation date (newest first)
            combinedResults.sort((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()));

            // Manual pagination
            int start = (int) pageable.getOffset();
            int end = Math.min((start + pageable.getPageSize()), combinedResults.size());
            List<CombinedLetterDTO> paged = combinedResults.subList(start, end);

            return new org.springframework.data.domain.PageImpl<>(paged, pageable, combinedResults.size());

        } catch (Exception e) {
            log.error("Unexpected error fetching combined letters with pagination: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to fetch letters due to internal error.", e);
        }
    }
}