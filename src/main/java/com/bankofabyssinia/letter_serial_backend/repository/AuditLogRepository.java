package com.bankofabyssinia.letter_serial_backend.repository;

import com.bankofabyssinia.letter_serial_backend.entity.AuditLog;
import com.bankofabyssinia.letter_serial_backend.enums.EntityType;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    List<AuditLog> findByEntityTypeAndEntityId(EntityType entityType, Long entityId);
}