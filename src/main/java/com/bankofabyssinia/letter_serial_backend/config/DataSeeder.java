package com.bankofabyssinia.letter_serial_backend.config;

import com.bankofabyssinia.letter_serial_backend.entity.District;
import com.bankofabyssinia.letter_serial_backend.entity.DistrictSequence;
import com.bankofabyssinia.letter_serial_backend.entity.Branch;
import com.bankofabyssinia.letter_serial_backend.entity.User;
import com.bankofabyssinia.letter_serial_backend.repository.DistrictRepository;
import
com.bankofabyssinia.letter_serial_backend.repository.DistrictSequenceRepository;
import com.bankofabyssinia.letter_serial_backend.repository.BranchRepository;
import com.bankofabyssinia.letter_serial_backend.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.dao.OptimisticLockingFailureException;

import java.util.concurrent.atomic.AtomicBoolean;

// import java.util.Optional;

@Component
public class DataSeeder {

private static final org.slf4j.Logger log =
org.slf4j.LoggerFactory.getLogger(DataSeeder.class);

private final DistrictRepository districtRepository;
private final DistrictSequenceRepository sequenceRepository;
@SuppressWarnings("unused")
private final BranchRepository branchRepository;
private final UserRepository userRepository;
private final PasswordEncoder passwordEncoder;

	private static final AtomicBoolean seeded = new AtomicBoolean(false);

public DataSeeder(DistrictRepository districtRepository,
DistrictSequenceRepository sequenceRepository,
BranchRepository branchRepository,
UserRepository userRepository,
PasswordEncoder passwordEncoder) {
this.districtRepository = districtRepository;
this.sequenceRepository = sequenceRepository;
this.branchRepository = branchRepository;
this.userRepository = userRepository;
this.passwordEncoder = passwordEncoder;
}

@EventListener
@Transactional
public void seedData(ApplicationReadyEvent event) {
	if (!seeded.compareAndSet(false, true)) {
		log.info("DataSeeder already executed in this JVM — skipping duplicate run");
		return;
	}
	seedAdminData();
}

private void seedAdminData() {
// Safety check: skip if admin already exists
if
(userRepository.findByEmail("yihun.shekuri@bankofabyssinia.com").isPresent())
{
log.info("Admin user already exists → skipping initial data seeding");
return;
}

// Extra safety: skip if Head Office already exists
if (districtRepository.existsByCode("HO")) {
log.info("Head Office district already exists → skipping seeding");
return;
}

log.info("=== STARTING INITIAL ADMIN DATA SEEDING ===");

// 1. Create Head District
District headDistrict = new District(
"Head Office",
"HO",
"Main headquarters and administrative center",
true);

// 2. Create default Auxiliary Branch and link it bidirectionally
Branch auxiliaryBranch = new Branch(
"Auxiliary Office",
"AUX-OFF",
false, // not default (you can change this if needed)
null // district will be set via addBranch
);

// Important: add branch to district's collection → cascade will handle
// @Persist
headDistrict.addBranch(auxiliaryBranch);

	// 3. Save district → branch is cascaded automatically
	District savedHead;
	try {
		savedHead = districtRepository.save(headDistrict);
	} catch (OptimisticLockingFailureException ex) {
		log.warn("Optimistic locking on district save — another process modified the row, reloading existing district");
		// Try to load existing district by code if save failed due to concurrent modification
		savedHead = districtRepository.findByCode("HO").orElseThrow(() -> ex);
	}
log.info("Seeded district: {} (code: {}) with default branch: {}",
savedHead.getName(), savedHead.getCode(), auxiliaryBranch.getName());

// 4. Create sequence for the district
DistrictSequence seqHead = new DistrictSequence(savedHead, 0L);
	try {
		sequenceRepository.save(seqHead);
	} catch (OptimisticLockingFailureException ex) {
		log.warn("Optimistic locking when saving district sequence — skipping creation");
	}
log.info("Created sequence for district: {}", savedHead.getCode());

// 5. Create admin user
User ownerAdmin = new User();
ownerAdmin.setName("Yihun Shekuri");
ownerAdmin.setEmail("yihun.shekuri@bankofabyssinia.com");
ownerAdmin.setPassword(passwordEncoder.encode("123456"));
ownerAdmin.setRole("ROLE_ADMIN");
ownerAdmin.setDistrict(savedHead);
ownerAdmin.setBranch(auxiliaryBranch); // use the persisted branch
ownerAdmin.setActive(true);
ownerAdmin.setFirstTime(false);

	try {
		userRepository.save(ownerAdmin);
	} catch (OptimisticLockingFailureException ex) {
		log.warn("Optimistic locking when saving admin user — skipping creation");
	}

log.info("Seeded admin user: {} <{}>", ownerAdmin.getName(),
ownerAdmin.getEmail());

log.info("=== INITIAL ADMIN DATA SEEDING COMPLETED SUCCESSFULLY ===");
log.info("You can now log in as admin and create more districts/branches from the frontend.");
}
}

