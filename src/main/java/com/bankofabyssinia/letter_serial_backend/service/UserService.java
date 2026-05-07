package com.bankofabyssinia.letter_serial_backend.service;

import com.bankofabyssinia.letter_serial_backend.dto.UserDTO;
import com.bankofabyssinia.letter_serial_backend.entity.Branch;
import com.bankofabyssinia.letter_serial_backend.entity.District;
import com.bankofabyssinia.letter_serial_backend.entity.User;
import com.bankofabyssinia.letter_serial_backend.repository.BranchRepository;
import com.bankofabyssinia.letter_serial_backend.repository.DistrictRepository;
import com.bankofabyssinia.letter_serial_backend.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);
    private static final int RESET_TOKEN_EXPIRY_MINUTES = 30;

    private final UserRepository userRepository;
    private final DistrictRepository districtRepository;
    private final BranchRepository branchRepository;
    private final PasswordEncoder passwordEncoder;
    private final OtpService otpService;

    public UserService(UserRepository userRepository,
            DistrictRepository districtRepository,
            BranchRepository branchRepository,
            PasswordEncoder passwordEncoder,
            OtpService otpService) {
        this.userRepository = userRepository;
        this.districtRepository = districtRepository;
        this.branchRepository = branchRepository;
        this.passwordEncoder = passwordEncoder;
        this.otpService = otpService;
    }

    @Transactional
    @SuppressWarnings("UnnecessaryUnboxing")
    public UserDTO createUser(UserDTO dto) {
        if (dto.getDistrictId() == null) {
            throw new IllegalArgumentException("District ID is required");
        }
        District district = districtRepository.findById(dto.getDistrictId())
                .orElseThrow(() -> new IllegalArgumentException("District not found"));

        String normalizedEmail = dto.getEmail().trim().toLowerCase();
        if (userRepository.findByEmail(normalizedEmail).isPresent()) {
            throw new IllegalArgumentException("Email already exists");
        }

        if (dto.getBranchId() == null || dto.getBranchId().trim().isEmpty()) {
            throw new IllegalArgumentException("Branch ID is required");
        }
        Branch branch = branchRepository.findById(dto.getBranchId())
                .orElseThrow(() -> new IllegalArgumentException("Branch not found"));

        if (!branch.getDistrict().getId().equals(district.getId())) {
            throw new IllegalArgumentException("Branch does not belong to the specified district");
        }

        User user = new User();
        user.setName(dto.getName().trim());
        user.setEmail(normalizedEmail);
        user.setRole(dto.getRole());
        user.setDisplayRole(dto.getDisplayRole());
        user.setDistrict(district);
        user.setBranch(branch);
        user.setActive(dto.getIsActive() == null ? true : dto.getIsActive().booleanValue());
        user.setFirstTime(true);
        user.setCreatedAt(LocalDateTime.now());

        if (dto.getPassword() == null || dto.getPassword().trim().isEmpty()) {
            throw new IllegalArgumentException("Password is required for new user");
        }
        user.setPassword(passwordEncoder.encode(dto.getPassword().trim()));

        User saved = userRepository.save(user);
        log.info("User created: {}", saved.getEmail());
        return toDto(saved);
    }

    // ────────────────────────────────────────────────
    // PASSWORD RESET
    // ────────────────────────────────────────────────

    @Transactional
    public boolean initiatePasswordReset(String email, String deviceFingerprint) {
        String normalizedEmail = email.trim().toLowerCase();
        boolean realUserFoundAndActive = false;
        User user = null;
        String resetSessionId = UUID.randomUUID().toString();

        try {
            Optional<User> opt = userRepository.findByEmail(normalizedEmail);
            if (opt.isPresent()) {
                user = opt.get();
                if (user.isActive()) {
                    realUserFoundAndActive = true;
                    user.setPasswordResetSessionId(resetSessionId);
                    user.setPasswordResetExpiresAt(LocalDateTime.now().plusMinutes(RESET_TOKEN_EXPIRY_MINUTES));
                    userRepository.save(user);
                }
            }

            if (realUserFoundAndActive && user != null) {
                otpService.generateOtp(normalizedEmail, normalizedEmail, resetSessionId,
                        deviceFingerprint != null ? deviceFingerprint : "unknown");
            } else {
                otpService.generateDummyOtp(); // timing protection
            }

            log.info("Password reset requested for: {}", normalizedEmail);
            return true;

        } catch (Exception e) {
            log.error("Unexpected error during reset initiation for {}: {}", normalizedEmail, e.getMessage(), e);
            return true; // never reveal failure to client
        }
    }

    @Transactional
    public boolean completePasswordReset(String email, String otpCode, String resetSessionId, String newPassword) {
        if (otpCode == null || resetSessionId == null || newPassword == null || newPassword.trim().isEmpty()) {
            return false;
        }

        String normalizedEmail = email.trim().toLowerCase();

        try {
            Optional<User> opt = userRepository.findByEmail(normalizedEmail);
            if (opt.isEmpty()) {
                return false;
            }

            User user = opt.get();

            if (!resetSessionId.equals(user.getPasswordResetSessionId())) {
                return false;
            }

            if (user.getPasswordResetExpiresAt() == null ||
                    user.getPasswordResetExpiresAt().isBefore(LocalDateTime.now())) {
                user.setPasswordResetSessionId(null);
                user.setPasswordResetExpiresAt(null);
                userRepository.save(user);
                return false;
            }

            boolean validOtp = otpService.validateOtp(normalizedEmail, otpCode.trim(), resetSessionId);
            if (!validOtp) {
                return false;
            }

            user.setPassword(passwordEncoder.encode(newPassword.trim()));
            user.setPasswordResetSessionId(null);
            user.setPasswordResetExpiresAt(null);
            user.setFirstTime(false);
            userRepository.save(user);

            otpService.invalidateOtp(normalizedEmail, resetSessionId);

            log.info("Password successfully reset for: {}", normalizedEmail);
            return true;

        } catch (Exception e) {
            log.error("Error during password reset completion for {}: {}", normalizedEmail, e.getMessage(), e);
            return false;
        }
    }

    // ────────────────────────────────────────────────
    // OTHER METHODS
    // ────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<UserDTO> getAllUsers() {
        return userRepository.findAll().stream().map(this::toDto).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Optional<UserDTO> getUserById(String id) {
        return userRepository.findById(id).map(this::toDto);
    }

    @SuppressWarnings("null")
    @Transactional
    public UserDTO updateUser(String id, UserDTO dto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        String newEmail = dto.getEmail() != null ? dto.getEmail().trim().toLowerCase() : null;
        if (newEmail != null && !newEmail.equals(user.getEmail())) {
            if (userRepository.findByEmail(newEmail)
                    .filter(u -> !u.getId().equals(id)).isPresent()) {
                throw new IllegalArgumentException("Email already in use");
            }
            user.setEmail(newEmail);
        }

        if (dto.getName() != null)
            user.setName(dto.getName().trim());
        if (dto.getRole() != null)
            user.setRole(dto.getRole());
        if (dto.getDisplayRole() != null)
            user.setDisplayRole(dto.getDisplayRole());
        if (dto.getIsActive() != null)
            user.setActive(dto.getIsActive());

        if (dto.getDistrictId() != null) {
            District district = districtRepository.findById(dto.getDistrictId())
                    .orElseThrow(() -> new IllegalArgumentException("District not found"));
            user.setDistrict(district);

            if (dto.getBranchId() == null) {
                throw new IllegalArgumentException("Branch ID required when changing district");
            }
        }

        if (dto.getBranchId() != null) {
            Branch branch = branchRepository.findById(dto.getBranchId())
                    .orElseThrow(() -> new IllegalArgumentException("Branch not found"));
            if (!branch.getDistrict().getId().equals(user.getDistrict().getId())) {
                throw new IllegalArgumentException("Branch does not belong to user's district");
            }
            user.setBranch(branch);
        }

        if (dto.getPassword() != null && !dto.getPassword().trim().isEmpty()) {
            user.setPassword(passwordEncoder.encode(dto.getPassword().trim()));
        }

        return toDto(userRepository.save(user));
    }

    @Transactional(readOnly = true)
    public Optional<String> findBranchIdByEmail(String email) {
        if (email == null || email.trim().isBlank()) {
            return Optional.empty();
        }
        return userRepository.findBranchIdByEmail(email.trim().toLowerCase());
    }

    @Transactional(readOnly = true)
    public List<UserDTO> getAllUsersWithDetails() {
        List<User> users = userRepository.findAll();
        return users.stream().map(this::toDto).collect(Collectors.toList());
    }

    @Transactional
    public UserDTO freezeUser(String id, boolean freeze) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        user.setActive(!freeze);
        return toDto(userRepository.save(user));
    }

    @Transactional
    public UserDTO activateUser(String id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        user.setActive(true);
        return toDto(userRepository.save(user));
    }

    @Transactional
    public void softDeleteUser(String id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        user.setActive(false);
        userRepository.save(user);
    }

    private UserDTO toDto(User u) {
        UserDTO dto = new UserDTO();
        dto.setId(u.getId());
        dto.setName(u.getName());
        dto.setEmail(u.getEmail());
        dto.setRole(u.getRole());
        dto.setDisplayRole(u.getDisplayRole());
        dto.setIsActive(u.isActive());
        dto.setIsFirstTime(u.isFirstTime());
        dto.setCreatedAt(u.getCreatedAt());
        if (u.getDistrict() != null) {
            dto.setDistrictId(u.getDistrict().getId());
            dto.setDistrictName(u.getDistrict().getName());
        }
        if (u.getBranch() != null) {
            dto.setBranchId(u.getBranch().getId());
            dto.setBranchName(u.getBranch().getName());
        }
        return dto;
    }
}